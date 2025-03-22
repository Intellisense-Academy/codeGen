package com.mcode.llp.codegen.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcode.llp.codegen.databases.OpenSearchClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenServiceTest {

    @Mock
    private OpenSearchClient openSearchClient;

    @Mock
    private JsonSchemaValidationService service;

    @Mock
    private UserService userService;

    @InjectMocks
    private GenService genService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        genService = new GenService(openSearchClient, userService, service);
    }

    @Test
    void testIndexExists() throws IOException, InterruptedException {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(openSearchClient.sendRequest(anyString(), eq("HEAD"), isNull())).thenReturn(mockResponse);
        assertTrue(genService.indexExists("testIndex"));
    }

    @Test
    void testInsertData() throws IOException, InterruptedException {
        String username = "testUser";
        String password = "testPass";
        String schemaName = "testSchema";
        JsonNode data = objectMapper.readTree("{\"name\":\"test\"}");

        when(userService.isValidUser(username, password, schemaName, "POST"))
                .thenReturn(ResponseEntity.ok(new HashMap<String, Object>() {{ put("tenant", "testTenant"); }}));

        when(service.validateJson(any(JsonNode.class), eq(schemaName)))
                .thenReturn(Collections.emptySet());

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.body()).thenReturn("{\"result\":\"created\"}");
        when(openSearchClient.sendRequest(anyString(), eq("POST"), anyString())).thenReturn(mockResponse);

        ResponseEntity<Object> response = genService.insertData(username, password, schemaName, data);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void testDeleteData() throws IOException, InterruptedException {
        String username = "testUser";
        String password = "testPass";
        String entityName = "testEntity";
        String documentId = UUID.randomUUID().toString();

        when(userService.isValidUser(username, password, entityName, "DELETE"))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = genService.deleteData(username, password, entityName, documentId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}
