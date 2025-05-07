package com.mcode.llp.codegen.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcode.llp.codegen.convertors.QueryGenerator;
import com.mcode.llp.codegen.databases.OpenSearchClient;
import com.mcode.llp.codegen.models.*;

import com.mcode.llp.codegen.notification.WhatAppNotification;
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

    @Mock
    private WhatAppNotification notification;

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

        JsonNode result = openSearchService.executeSearch(payload, tenantId);

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

        JsonNode result = openSearchService.executeSearch(payload, "tenant");
        assertNull(result);
    }

    @Test
    void testExtractFieldsWithWildcard() throws Exception {
        SearchRequestPayload payload = createSamplePayload(false);
        String query = "{}";

        when(generator.buildComplexQuery(any(), any())).thenReturn(query);
        when(openSearchClient.sendRequest(any(), any(), any())).thenReturn(mockResponse);
        when(mockResponse.body()).thenReturn(mockHitJson("1", Map.of("name", "abc", "email", "abc@mail.com")));

        JsonNode result = openSearchService.executeSearch(payload, "tenant");

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

        JsonNode result = openSearchService.executeSearch(payload, "tenant");

        assertNotNull(result);
        assertEquals("abc", result.get(0).get("name").asText());
        assertEquals("abc@mail.com", result.get(0).get("email").asText());

        assertFalse(result.get(0).has("_id"));
    }

    @Test
    void testSendNotification_success() throws Exception {
        // Arrange
        String tenantId = "testTenant";
        String name = "welcome";
        SearchRequestPayload payload = new SearchRequestPayload();
        SearchQuery mainQuery = new SearchQuery();
        mainQuery.setIndexName("contributors");
        mainQuery.setFieldsToReturn(List.of("name", "phno", "tenant"));
        mainQuery.setConditionGroups(List.of(new ConditionGroup())); // dummy
        payload.setMainQuery(mainQuery);

        // This is the format that your service expects from openSearchClient for contributors
        String fakeContributorsJson = """
    {
      "hits": {
        "hits": [
          {
            "_source": {
              "name": "Mukesh Raj",
              "phno": "918778097615",
              "tenant": "temple"
            }
          },
          {
            "_source": {
              "name": "Karthick",
              "phno": "919383467779",
              "tenant": "temple"
            }
          }
        ]
      }
    }
    """;

        // This is the format that your service expects for the template
        String templateJson = """
    {
      "hits": {
        "hits": [
          {
            "_source": {
              "content": "Hi ${name},\\nThis is a reminder that your monthly contribution is still unpaid. Kindly complete the payment at your earliest convenience.\\nIf you’ve already paid, please ignore this message.\\n\\nThank you,\\n${tenant} Team"
            }
          }
        ]
      }
    }
    """;

        // Mocks
        when(generator.buildComplexQuery(any(), eq(tenantId))).thenReturn("{ \"query\": { \"match_all\": {} } }");
        when(openSearchClient.sendRequest(contains("contributors/_search"), eq("POST"), anyString()))
                .thenReturn(mockResponse);
        when(openSearchClient.sendRequest(contains("/notification/_search?q=name:" + name), eq("GET"), isNull()))
                .thenReturn(mockResponse);

        // Simulate different responses for different calls
        when(mockResponse.body())
                .thenReturn(fakeContributorsJson) // first call (contributors)
                .thenReturn(templateJson);       // second call (template)

        when(notification.sendWhatsAppMessage(anyString(), anyString())).thenReturn(true);

        // Act
        List<Map<String, String>> results = openSearchService.sendNotification(payload, tenantId, name);

        // Assert
        assertEquals(2, results.size());
        assertEquals("Message sent successfully", results.get(0).get("status"));
        assertEquals("Message sent successfully", results.get(1).get("status"));
        verify(notification, times(2)).sendWhatsAppMessage(anyString(), contains("Hi"));
    }

    @Test
    void testSendNotification_templateMissing() throws Exception {
        SearchRequestPayload payload = new SearchRequestPayload();
        SearchQuery mainQuery = new SearchQuery();
        mainQuery.setIndexName("contributors");
        mainQuery.setFieldsToReturn(List.of("name", "phno", "tenant"));
        mainQuery.setConditionGroups(List.of(new ConditionGroup())); // dummy
        payload.setMainQuery(mainQuery);

        String fakeSearchResult = "[]";
        String emptyTemplateJson = """
        {
          "hits": {
            "hits": []
          }
        }
        """;

        when(generator.buildComplexQuery(any(), any())).thenReturn("{ \"query\": { \"match_all\": {} } }");
        when(openSearchClient.sendRequest(anyString(), anyString(), any()))
                .thenReturn(mockResponse);
        when(mockResponse.body()).thenReturn(fakeSearchResult).thenReturn(emptyTemplateJson);

        List<?> result = openSearchService.sendNotification(payload, "testTenant", "welcome");

        assertTrue(result.isEmpty());
    }

}
