package com.mcode.llp.codegen.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.mcode.llp.codegen.databases.OpenSearchClient;
import com.mcode.llp.codegen.models.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchemaServiceTest {

    @Mock
    private OpenSearchClient openSearchClient;

    @InjectMocks
    private SchemaService schemaService;

    @Mock
    private HttpResponse<String> mockHttpResponse;


    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
     void testInsertSchema() throws Exception {
        Schema schema = new Schema();
        schema.setTitle("TestSchema");

        when(openSearchClient.sendRequest(anyString(), eq("POST"), anyString()))
                .thenReturn(mockHttpResponse);

        HttpResponse<String> response = schemaService.insertSchema("schemas", "TestSchema", schema);
        assertNotNull(response);
        verify(openSearchClient, times(1)).sendRequest(contains("/schemas/_doc/TestSchema"), eq("POST"), anyString());
    }

    @Test
    void testUpdateSchema() throws Exception {
        Schema schema = new Schema();
        schema.setTitle("UpdateTest");

        when(openSearchClient.sendRequest(anyString(), eq("POST"), anyString()))
                .thenReturn(mockHttpResponse);

        HttpResponse<String> response = schemaService.updateSchema("UpdateTest", schema);
        assertNotNull(response);
        verify(openSearchClient).sendRequest(contains("/schemas/_update/UpdateTest"), eq("POST"), anyString());
    }

    @Test
    void testGetAllSchema() throws Exception {
        String mockResponse = """
        {
            "hits": {
                "hits": [
                    { "_source": { "title": "Schema1" } },
                    { "_source": { "title": "Schema2" } }
                ]
            }
        }
        """;

        when(mockHttpResponse.body()).thenReturn(mockResponse);
        when(openSearchClient.sendRequest(anyString(), eq("GET"), isNull()))
                .thenReturn(mockHttpResponse);

        JsonNode result = schemaService.getAllSchema();
        assertEquals(2, result.size());
        assertEquals("Schema1", result.get(0).get("title").asText());
    }

    @Test
    void testGetSchema_Found() throws Exception {
        String mockResponse = """
        {
            "_source": { "title": "FoundSchema" }
        }
        """;

        when(mockHttpResponse.body()).thenReturn(mockResponse);
        when(openSearchClient.sendRequest(anyString(), eq("GET"), isNull()))
                .thenReturn(mockHttpResponse);

        JsonNode result = schemaService.getSchema("FoundSchema");
        assertNotNull(result);
        assertEquals("FoundSchema", result.get("title").asText());
    }

    @Test
    void testGetSchema_NotFound() throws Exception {
        String mockResponse = "{}";

        when(mockHttpResponse.body()).thenReturn(mockResponse);
        when(openSearchClient.sendRequest(anyString(), eq("GET"), isNull()))
                .thenReturn(mockHttpResponse);

        JsonNode result = schemaService.getSchema("NotFound");
        assertNull(result);
    }

    @Test
    void testDeleteSchema() throws Exception {
        when(openSearchClient.sendRequest(contains("/schemas/_doc/DeleteMe"), eq("DELETE"), isNull()))
                .thenReturn(mockHttpResponse);

        HttpResponse<String> response = schemaService.deleteSchema("DeleteMe");
        assertNotNull(response);
        verify(openSearchClient).sendRequest(contains("/schemas/_doc/DeleteMe"), eq("DELETE"), isNull());
    }

    @Test
    void testCreateDefaultSettingForSchema() throws Exception {
        when(openSearchClient.sendRequest(contains("/settings/_doc/MySchema"), eq("POST"), anyString()))
                .thenReturn(mockHttpResponse);

        HttpResponse<String> response = schemaService.createDefaultSettingForSchema("MySchema");
        assertNotNull(response);
        verify(openSearchClient).sendRequest(contains("/settings/_doc/MySchema"), eq("POST"), anyString());
    }
}
