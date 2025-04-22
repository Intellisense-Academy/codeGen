package com.mcode.llp.codegen.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcode.llp.codegen.convertors.QueryGenerator;
import com.mcode.llp.codegen.databases.OpenSearchClient;
import com.mcode.llp.codegen.models.Condition;
import com.mcode.llp.codegen.models.ConditionGroup;
import com.mcode.llp.codegen.models.SearchRequestPayload;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OpenSearchServiceTest {

    @Mock
    private OpenSearchClient openSearchClient;

    @Mock
    private QueryGenerator generator;

    @Mock
    private HttpResponse<String> mockResponse;

    @InjectMocks
    private OpenSearchService openSearchService;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private SearchRequestPayload createSamplePayload(boolean withRelation) {
        // Main condition
        Condition condition = new Condition();
        condition.setField("name");
        condition.setOperator("eq");
        condition.setValue("aswin");

        ConditionGroup group = new ConditionGroup();
        group.setType("and");
        group.setConditions(List.of(condition));

        SearchRequestPayload payload = new SearchRequestPayload();
        payload.setIndexName("contributor");
        payload.setConditionGroups(List.of(group));
        payload.setFieldsToReturn(List.of("*"));

        if (withRelation) {
            // Related conditions
            Condition c1 = new Condition();
            c1.setField("date");
            c1.setOperator("gte");
            c1.setValue("2025-04-01");

            Condition c2 = new Condition();
            c2.setField("date");
            c2.setOperator("lt");
            c2.setValue("2025-05-01");

            ConditionGroup relatedGroup = new ConditionGroup();
            relatedGroup.setType("and");
            relatedGroup.setConditions(List.of(c1, c2));

            SearchRequestPayload relatedPayload = new SearchRequestPayload();
            relatedPayload.setIndexName("transaction");
            relatedPayload.setConditionGroups(List.of(relatedGroup));
            relatedPayload.setFieldsToReturn(List.of("contributorId"));

            payload.setRelation("not in");
            payload.setRelatedQuery(relatedPayload);
        }

        return payload;
    }

    private String mockHitJson(String id, Map<String, String> sourceFields) throws Exception {
        ObjectNode hit = mapper.createObjectNode();
        hit.put("_id", id);
        ObjectNode source = hit.putObject("_source");
        for (Map.Entry<String, String> entry : sourceFields.entrySet()) {
            source.put(entry.getKey(), entry.getValue());
        }
        return mapper.writeValueAsString(Map.of("hits", Map.of("hits", List.of(hit))));
    }

    @Test
    void testExecuteSearchWithoutRelation() throws Exception {
        SearchRequestPayload payload = createSamplePayload(false);
        String queryJson = "{\"query\":{\"match_all\":{}}}";
        String tenantId = "tenant1";

        when(generator.buildComplexQuery(payload.getConditionGroups(), tenantId)).thenReturn(queryJson);
        when(openSearchClient.sendRequest(anyString(), eq("POST"), eq(queryJson))).thenReturn(mockResponse);

        String body = mockHitJson("123", Map.of("name", "aswin"));
        when(mockResponse.body()).thenReturn(body);

        JsonNode result = openSearchService.executeSearch(payload, tenantId, List.of("*"));

        assertNotNull(result);
        assertEquals("aswin", result.get(0).get("name").asText());
        assertEquals("123", result.get(0).get("_id").asText());
    }

    @Test
    void testSearchOpenSearchHandlesException() throws Exception {
        String query = "{}";
        SearchRequestPayload payload = createSamplePayload(false);
        String tenantId = "tenant";

        when(generator.buildComplexQuery(any(), eq(tenantId))).thenReturn(query);

        when(openSearchClient.sendRequest(anyString(), anyString(), eq(query)))
                .thenThrow(new IOException("Simulated error"));

        JsonNode result = openSearchService.executeSearch(payload, tenantId, List.of("name"));

        assertNull(result);
    }


    @Test
    void testExtractFieldsWithWildcard() throws Exception {
        SearchRequestPayload payload = createSamplePayload(false);
        String query = "{}";

        when(generator.buildComplexQuery(any(), any())).thenReturn(query);
        when(openSearchClient.sendRequest(any(), any(), any())).thenReturn(mockResponse);
        when(mockResponse.body()).thenReturn(mockHitJson("1", Map.of("name", "abc", "email", "abc@mail.com")));

        JsonNode result = openSearchService.executeSearch(payload, "tenant", List.of("*"));

        assertNotNull(result);
        assertEquals("abc", result.get(0).get("name").asText());
        assertEquals("abc@mail.com", result.get(0).get("email").asText());
    }

    @Test
    void testExtractFieldsWithoutWildcard() throws Exception {
        SearchRequestPayload payload = createSamplePayload(false);
        String query = "{}";

        when(generator.buildComplexQuery(any(), any())).thenReturn(query);
        when(openSearchClient.sendRequest(any(), any(), any())).thenReturn(mockResponse);
        when(mockResponse.body()).thenReturn(mockHitJson("1", Map.of("name", "abc", "email", "abc@mail.com")));

        JsonNode result = openSearchService.executeSearch(payload, "tenant", List.of("name", "_id"));

        assertNotNull(result);
        assertEquals("abc", result.get(0).get("name").asText());
        assertEquals("1", result.get(0).get("_id").asText());
        assertNull(result.get(0).get("email"));
    }

    @Test
    void testAddAllFieldsFromSource() throws Exception {
        SearchRequestPayload payload = createSamplePayload(false);
        String query = "{}";

        when(generator.buildComplexQuery(any(), any())).thenReturn(query);
        when(openSearchClient.sendRequest(any(), any(), any())).thenReturn(mockResponse);

        // Simulate a hit with multiple fields
        when(mockResponse.body()).thenReturn(mockHitJson("1", Map.of(
                "name", "aswin",
                "email", "aswin@mail.com",
                "age", "30"
        )));

        JsonNode result = openSearchService.executeSearch(payload, "tenant", List.of("*"));

        assertNotNull(result);
        JsonNode resultNode = result.get(0);
        assertEquals("aswin", resultNode.get("name").asText());
        assertEquals("aswin@mail.com", resultNode.get("email").asText());
        assertEquals("30", resultNode.get("age").asText());
        assertEquals("1", resultNode.get("_id").asText()); // _id should also be present
    }

    @Test
    void testAddSelectedFields() throws Exception {
        SearchRequestPayload payload = createSamplePayload(false);
        String query = "{}";

        when(generator.buildComplexQuery(any(), any())).thenReturn(query);
        when(openSearchClient.sendRequest(any(), any(), any())).thenReturn(mockResponse);

        // Simulate a hit with multiple fields
        when(mockResponse.body()).thenReturn(mockHitJson("2", Map.of(
                "name", "arjun",
                "email", "arjun@mail.com",
                "age", "25"
        )));

        JsonNode result = openSearchService.executeSearch(payload, "tenant", List.of("name", "age", "_id"));

        assertNotNull(result);
        JsonNode resultNode = result.get(0);
        assertEquals("arjun", resultNode.get("name").asText());
        assertEquals("25", resultNode.get("age").asText());
        assertEquals("2", resultNode.get("_id").asText());
        assertNull(resultNode.get("email")); // Not selected
    }

}
