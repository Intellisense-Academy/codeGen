package com.mcode.llp.codegen.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.mcode.llp.codegen.models.Schema;
import com.mcode.llp.codegen.databases.OpenSearchClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchemaServiceTest {

    @Mock
    private OpenSearchClient openSearchClient;

    @InjectMocks
    private SchemaService schemaService;

    @BeforeEach
    void setUp() {
        schemaService = new SchemaService(openSearchClient);
    }

    @Test
    void testInsertSchema() throws IOException, InterruptedException {
        Schema schemaData = new Schema();
        String index = "testIndex";
        String id = "testId";

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(openSearchClient.sendRequest(anyString(), eq("POST"), anyString())).thenReturn(mockResponse);

        HttpResponse<String> response = schemaService.insertSchema(index, id, schemaData);
        assertNotNull(response);
    }

    @Test
    void testUpdateSchema() throws IOException, InterruptedException {
        Schema schemaData = new Schema();
        String id = "testId";

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(openSearchClient.sendRequest(anyString(), eq("POST"), anyString())).thenReturn(mockResponse);

        HttpResponse<String> response = schemaService.updateSchema(id, schemaData);
        assertNotNull(response);
    }

    @Test
    void testGetAllSchema() throws IOException, InterruptedException {
        String jsonResponse = "{\"hits\":{\"hits\":[{\"_source\":{\"name\":\"TestSchema\"}}]}}";
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(openSearchClient.sendRequest(anyString(), eq("GET"), isNull())).thenReturn(mockResponse);

        JsonNode response = schemaService.getAllSchema();
        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    void testGetSchema() throws IOException, InterruptedException {
        String entityName = "testSchema";
        String jsonResponse = "{\"_source\":{\"name\":\"TestSchema\"}}";
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(openSearchClient.sendRequest(anyString(), eq("GET"), isNull())).thenReturn(mockResponse);

        JsonNode response = schemaService.getSchema(entityName);
        assertNotNull(response);
        assertEquals("TestSchema", response.get("name").asText());
    }

    @Test
    void testDeleteSchema() throws IOException, InterruptedException {
        String id = "testId";

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(openSearchClient.sendRequest(anyString(), eq("DELETE"), isNull())).thenReturn(mockResponse);

        HttpResponse<String> response = schemaService.deleteSchema(id);
        assertNotNull(response);
    }
}
