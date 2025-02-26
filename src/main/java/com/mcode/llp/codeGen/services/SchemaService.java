package com.mcode.llp.codegen.services;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcode.llp.codegen.models.Schema;
import com.mcode.llp.codegen.databases.OpenSearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.*;

@Service
public class SchemaService {

    private static final String ACTION_1 = "_source";
    private final OpenSearchClient openSearchClient;

    @Autowired
    public SchemaService(OpenSearchClient openSearchClient) {
        this.openSearchClient = openSearchClient;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    public HttpResponse<String> insertSchema(String index, String id, Schema schemaData) throws IOException,InterruptedException {
        String jsonData = objectMapper.writeValueAsString(schemaData);
        String endpoint = "/" + index + "/_doc/" + id;
        return openSearchClient.sendRequest(endpoint, "POST", jsonData);
    }

    public HttpResponse<String> updateSchema(String id, Schema schemaData) throws IOException,InterruptedException {
        String jsonData = objectMapper.writeValueAsString(Map.of("doc", schemaData));
        String endpoint = "/schemas/_update/" + id;
        return openSearchClient.sendRequest(endpoint, "POST", jsonData);
    }


    public JsonNode getAllSchema() throws IOException,InterruptedException {
        String endpoint = "/schemas/_search?filter_path=hits.hits._source";

        HttpResponse<String> response = openSearchClient.sendRequest(endpoint, "GET", null);
        JsonNode responseJson = objectMapper.readTree(response.body());

        JsonNode hitsArray = responseJson.at("/hits/hits");

        List<JsonNode> schemas = new ArrayList<>();
        for (JsonNode hit : hitsArray) {
            JsonNode sourceObject = hit.get(ACTION_1);
            schemas.add(sourceObject);
        }

        return objectMapper.valueToTree(schemas);
    }

    public JsonNode getSchema(String entityName) {
        String endpoint = "/schemas/_doc/" + entityName;
        try {
            HttpResponse<String> response = openSearchClient.sendRequest(endpoint, "GET", null);

            JsonNode responseJson = objectMapper.readTree(response.body());

            if (responseJson.has(ACTION_1)) {
                return responseJson.get(ACTION_1);
            } else {
                return null;
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public HttpResponse<String> deleteSchema(String id) throws IOException,InterruptedException {
        String endpoint = "/schemas/_doc/"+id;
        return openSearchClient.sendRequest(endpoint, "DELETE", null);
    }
}
