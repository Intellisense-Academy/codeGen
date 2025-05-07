package com.mcode.llp.codegen.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcode.llp.codegen.convertors.QueryGenerator;
import com.mcode.llp.codegen.databases.OpenSearchClient;
import com.mcode.llp.codegen.models.AggregationSpec;
import com.mcode.llp.codegen.models.SearchQuery;
import com.mcode.llp.codegen.models.SearchRequestPayload;
import com.mcode.llp.codegen.notification.WhatAppNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class OpenSearchService {
    private final OpenSearchClient openSearchClient;
    private final QueryGenerator generator;
    private final WhatAppNotification notification;
    private static final Logger logger = LoggerFactory.getLogger(OpenSearchService.class);
    HttpResponse<String> response;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public OpenSearchService(OpenSearchClient openSearchClient, QueryGenerator generator, WhatAppNotification notification){
        this.openSearchClient = openSearchClient;
        this.generator = generator;
        this.notification=notification;
    }

    public List<Map<String, String>> sendNotification(SearchRequestPayload payload, String tenantId, String name) throws InterruptedException, IOException {
        JsonNode requiredDatas = executeSearch(payload, tenantId);
        String endpoint = "/notification/_search?q=name:" + name;
        response = openSearchClient.sendRequest(endpoint, "GET", null);

        List<Map<String, String>> results = new ArrayList<>();

        JsonNode templateNode = mapper.readTree(response.body());
        JsonNode hitsArray = templateNode.path("hits").path("hits");

        if (hitsArray.isArray() && !hitsArray.isEmpty()) {
            JsonNode sourceNode = hitsArray.get(0).path("_source");
            String templateContent = sourceNode.path("content").asText(); // Message Template

            // Loop through each contributor
            if (requiredDatas.isArray()) {
                for (JsonNode contributor : requiredDatas) {
                    String contributorName = contributor.path("name").asText();
                    String phoneNumber = contributor.path("phno").asText();
                    String contributorTenant = contributor.path("tenant").asText();

                    String personalizedMessage = templateContent
                            .replace("${name}", contributorName)
                            .replace("${tenant}", contributorTenant);

                    boolean isSent = notification.sendWhatsAppMessage(phoneNumber, personalizedMessage);

                    Map<String, String> result = new HashMap<>();
                    result.put("name", contributorName);
                    result.put("phno", phoneNumber);
                    result.put("status", isSent ? "Message sent successfully" : "Failed to send message");

                    results.add(result);
                }
            }
        } else {
            logger.error("No notification template found for name:{} ",name);
        }

        return results;
    }


    public JsonNode executeSearch(SearchRequestPayload payload, String tenantId) throws JsonProcessingException {
        List<String> fieldsToReturn = payload.getMainQuery().getFieldsToReturn();
        // If there's no relation, run normal search
        if (payload.getRelation() == null || payload.getRelatedQuery() == null) {
            String queryJson = generator.buildComplexQuery(payload.getMainQuery().getConditionGroups(), tenantId);
            ObjectNode mainQuery = (ObjectNode) mapper.readTree(queryJson);
            addAggregationsIfPresent(mainQuery, payload.getAggregations());
            return searchOpenSearch(payload.getMainQuery().getIndexName(), String.valueOf(mainQuery), fieldsToReturn,payload.getAggregations());
        }

        // Step 1: Execute the second query to get values
        SearchQuery relatedPayload = payload.getRelatedQuery();
        String relatedQueryJson = generator.buildComplexQuery(relatedPayload.getConditionGroups(), tenantId);
        JsonNode relatedResult = searchOpenSearch(relatedPayload.getIndexName(), relatedQueryJson, relatedPayload.getFieldsToReturn(),payload.getAggregations());

        // Step 2: Extract values for "not in"
        Set<String> excludeValues = new HashSet<>();
        String requestKey = relatedPayload.getFieldsToReturn().get(0);
        if (relatedResult != null) {
            for (JsonNode obj : relatedResult) {
                excludeValues.add(obj.get(requestKey).asText());
            }
        }

        // Step 3: Add a "must_not terms" clause to the main query
        String mainQueryJson = generator.buildComplexQuery(payload.getMainQuery().getConditionGroups(), tenantId);
        ObjectNode mainQuery = (ObjectNode) mapper.readTree(mainQueryJson);
        ObjectNode boolNode = (ObjectNode) mainQuery.path("query").path("bool");

        ArrayNode termsArray;
        ObjectNode termsNode = mapper.createObjectNode();
        ArrayNode valuesArray = mapper.createArrayNode();
        excludeValues.forEach(valuesArray::add);
        String fieldToFilter = payload.getConnectedKey().endsWith(".keyword") ? payload.getConnectedKey() : payload.getConnectedKey() + ".keyword";
        termsNode.putObject("terms").set(fieldToFilter, valuesArray); // ensure keyword for exact match

        if (payload.getRelation().equalsIgnoreCase("not in")) {
            termsArray = boolNode.withArray("must_not");
        } else if (payload.getRelation().equalsIgnoreCase("in")) {
            termsArray = boolNode.withArray("filter");
        } else {
            throw new IllegalArgumentException("Unsupported relation: " + payload.getRelation());
        }

        termsArray.add(termsNode);

        addAggregationsIfPresent(mainQuery, payload.getAggregations());

        // Step 4: Execute modified main query
        return searchOpenSearch(payload.getMainQuery().getIndexName(), mainQuery.toString(), fieldsToReturn,payload.getAggregations());
    }

    private void addAggregationsIfPresent(ObjectNode mainQuery, List<AggregationSpec> aggregations) {
        if (aggregations != null && !aggregations.isEmpty()) {
            ObjectNode aggsNode = mapper.createObjectNode();
            for (AggregationSpec agg : aggregations) {
                ObjectNode fieldNode = mapper.createObjectNode();
                fieldNode.set("field", mapper.getNodeFactory().textNode(agg.getField()));

                ObjectNode aggTypeNode = mapper.createObjectNode();
                aggTypeNode.set(agg.getType(), fieldNode);

                aggsNode.set(agg.getName(), aggTypeNode);
            }
            mainQuery.set("aggs", aggsNode);
        }
    }

    private JsonNode searchOpenSearch(String indexName, String queryJson, List<String> fieldsToReturn, List<AggregationSpec> aggregations) {
        try {
            String endPoint = "/" + indexName + "/_search";
            response = openSearchClient.sendRequest(endPoint, "POST", queryJson);

            JsonNode responseBody = mapper.readTree(response.body());

            boolean hasAggregations = aggregations != null && !aggregations.isEmpty();

            // If no aggregations, just return filtered hits
            if (!hasAggregations) {
                JsonNode hitsArray = responseBody.path("hits").path("hits");
                List<Map<String, Object>> result = new ArrayList<>();
                for (JsonNode hit : hitsArray) {
                    result.add(extractFieldsFromHit(hit, fieldsToReturn));
                }
                return mapper.valueToTree(result);
            }

            // When aggregations are present:
            // Extract hits with fields
            JsonNode hitsArray = responseBody.path("hits").path("hits");
            List<Map<String, Object>> resultHits = new ArrayList<>();
            for (JsonNode hit : hitsArray) {
                resultHits.add(extractFieldsFromHit(hit, fieldsToReturn));
            }

            // Extract flat aggregation results
            JsonNode aggNode = responseBody.path("aggregations");
            ObjectNode flatAgg = mapper.createObjectNode();
            if (aggNode != null && aggNode.isObject()) {
                aggNode.fieldNames().forEachRemaining(name -> {
                    JsonNode valueNode = aggNode.get(name).path("value");
                    if (!valueNode.isMissingNode()) {
                        flatAgg.put(name, valueNode.asDouble());
                    }
                });
            }

            // Compose final result
            ObjectNode finalResult = mapper.createObjectNode();
            finalResult.set("hits", mapper.valueToTree(resultHits));
            finalResult.set("aggregations", flatAgg);

            return finalResult;

        } catch (IOException | InterruptedException e) {
            logger.error("OpenSearch search failed: " + e.getMessage(), e);
            Thread.currentThread().interrupt();
            return null;
        }
    }


    private Map<String, Object> extractFieldsFromHit(JsonNode hit, List<String> fieldsToReturn) {
        Map<String, Object> obj = new HashMap<>();
        JsonNode source = hit.path("_source");

        // Include _id if requested
        if (fieldsToReturn.contains("_id")) {
            obj.put("_id", hit.path("_id").asText());
        }

        if (isWildcardAll(fieldsToReturn)) {
            addAllFieldsFromSource(obj, source);
        } else {
            addSelectedFields(obj, source, fieldsToReturn);
        }

        return obj;
    }

    private boolean isWildcardAll(List<String> fieldsToReturn) {
        return fieldsToReturn.size() == 1 && "*".equals(fieldsToReturn.get(0));
    }

    private void addAllFieldsFromSource(Map<String, Object> obj, JsonNode source) {
        source.fieldNames().forEachRemaining(field -> obj.put(field, source.get(field).asText()));
    }

    private void addSelectedFields(Map<String, Object> obj, JsonNode source, List<String> fieldsToReturn) {
        for (String field : fieldsToReturn) {
            if (!field.equals("_id") && source.has(field)) {
                obj.put(field, source.get(field).asText());
            }
        }
    }


}
