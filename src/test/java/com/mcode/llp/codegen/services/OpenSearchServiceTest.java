package com.mcode.llp.codegen.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcode.llp.codegen.convertors.QueryGenerator;
import com.mcode.llp.codegen.databases.OpenSearchClient;
import com.mcode.llp.codegen.models.Condition;
import com.mcode.llp.codegen.models.ConditionGroup;
import com.mcode.llp.codegen.models.SearchQuery;
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
        Condition mainCond = new Condition("name", "eq", "aswin");
        ConditionGroup mainGroup = new ConditionGroup("and", List.of(mainCond));
        SearchQuery mainQuery = new SearchQuery("contributor", List.of("*"),List.of(mainGroup));

        SearchRequestPayload payload = new SearchRequestPayload();
        payload.setMainQuery(mainQuery);

        if (withRelation) {
            Condition c1 = new Condition("date", "gte", "2025-04-01");
            Condition c2 = new Condition("date", "lt", "2025-05-01");
            ConditionGroup relatedGroup = new ConditionGroup("and", List.of(c1, c2));
            SearchQuery relatedQuery = new SearchQuery("transaction", List.of("contributorId"), List.of(relatedGroup));

            payload.setRelation("not in");
            payload.setRelatedQuery(relatedQuery);
            payload.setConnectedKey("contributorId");
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

        when(generator.buildComplexQuery(payload.getMainQuery().getConditionGroups(), tenantId)).thenReturn(queryJson);

        // _id is now inside _source
        String body = mockHitJson("123", Map.of("name", "aswin", "_id", "123"));
        when(openSearchClient.sendRequest(anyString(), eq("POST"), eq(queryJson))).thenReturn(mockResponse);
        when(mockResponse.body()).thenReturn(body);

        JsonNode result = openSearchService.executeSearch(payload, tenantId, List.of("*"));

        assertNotNull(result);
        assertEquals("aswin", result.get(0).get("name").asText());
        assertEquals("123", result.get(0).get("_id").asText());  // now fetched from _source
    }

    @Test
    void testSearchOpenSearchHandlesException() throws Exception {
        SearchRequestPayload payload = createSamplePayload(false);
        String queryJson = "{}";

        when(generator.buildComplexQuery(any(), any())).thenReturn(queryJson);
        when(openSearchClient.sendRequest(any(), any(), any())).thenThrow(new IOException("Simulated error"));

        JsonNode result = openSearchService.executeSearch(payload, "tenant", List.of("name"));
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
}
