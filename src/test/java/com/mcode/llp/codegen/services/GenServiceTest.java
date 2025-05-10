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
import java.util.UUID;
import java.util.*;

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

    @Mock
    private JsonSchemaValidationService jsonSchemaValidationService;

    @Mock
    private HttpResponse<String> mockHttpResponse;

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

    @Test
    void testIndexExists_WhenIndexExists_ShouldReturnTrue() throws IOException, InterruptedException {
        when(openSearchClient.sendRequest("/test_index", "HEAD", null)).thenReturn(mockHttpResponse);
        when(mockHttpResponse.statusCode()).thenReturn(200);

        assertTrue(genService.indexExists("test_index"));
    }

    @Test
    void testIndexExists_WhenIndexDoesNotExist_ShouldReturnFalse() throws IOException, InterruptedException {
        when(openSearchClient.sendRequest("/test_index", "HEAD", null)).thenReturn(mockHttpResponse);
        when(mockHttpResponse.statusCode()).thenReturn(404);

        assertFalse(genService.indexExists("test_index"));
    }

    @Test
    void testInsertData_WhenInvalidUser_ShouldReturnUnauthorized() throws Exception {
        ResponseEntity<Object> mockUserResponse = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        when(userService.isValidUser("user", "pass", "schema", "POST")).thenReturn(mockUserResponse);

        ResponseEntity<Object> response = genService.insertData("user", "pass", "schema", objectMapper.createObjectNode());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testDeleteData_WhenValidUser_ShouldDeleteSuccessfully() throws Exception {
        ResponseEntity<Object> mockUserResponse = ResponseEntity.ok().build();
        when(userService.isValidUser("user", "pass", "entity", "DELETE")).thenReturn(mockUserResponse);

        ResponseEntity<Object> response = genService.deleteData("user", "pass", "entity", "docId");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(openSearchClient).sendRequest("/entity/_doc/docId", "DELETE", null);
    }

    @Test
    void testDeleteData_WhenInvalidUser_ShouldReturnUnauthorized() throws Exception {
        ResponseEntity<Object> mockUserResponse = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        when(userService.isValidUser("user", "pass", "entity", "DELETE")).thenReturn(mockUserResponse);

        ResponseEntity<Object> response = genService.deleteData("user", "pass", "entity", "docId");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGetSingleData_WhenValidUser_ShouldReturnData() throws Exception {
        ResponseEntity<Object> mockUserResponse = ResponseEntity.ok().build();
        when(userService.isValidUser("user", "pass", "entity", "GET")).thenReturn(mockUserResponse);

        String jsonResponse = "{\"_source\": {\"key\": \"value\"}}";
        when(openSearchClient.sendRequest("/entity/_doc/docId", "GET", null)).thenReturn(mockHttpResponse);
        when(mockHttpResponse.body()).thenReturn(jsonResponse);

        ResponseEntity<Object> response = genService.getSingleData("user", "pass", "entity", "docId");
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetAllData_WhenValidUser_ShouldReturnDataList() throws Exception {
        ResponseEntity<Object> mockUserResponse = ResponseEntity.ok(Map.of("tenant", "testTenant"));
        when(userService.isValidUser("user", "pass", "entity", "GET")).thenReturn(mockUserResponse);

        String jsonResponse = "{ \"hits\": { \"hits\": [{ \"_id\": \"1\", \"_source\": {\"key\": \"value\"} }] } }";
        when(openSearchClient.sendRequest("/entity/_search?q=tenant:testTenant", "GET", null)).thenReturn(mockHttpResponse);
        when(mockHttpResponse.body()).thenReturn(jsonResponse);

        ResponseEntity<Object> response = genService.getAllData("user", "pass", "entity");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdateData_WhenValidUser_ShouldUpdateSuccessfully() throws Exception {
        ResponseEntity<Object> mockUserResponse = ResponseEntity.ok().build();
        when(userService.isValidUser("user", "pass", "entity", "PUT")).thenReturn(mockUserResponse);

        String mockUpdateResponse = "{\"result\": \"updated\"}";
        when(openSearchClient.sendRequest(anyString(), eq("POST"), anyString())).thenReturn(mockHttpResponse);
        when(mockHttpResponse.body()).thenReturn(mockUpdateResponse);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("field", "newValue");

        ResponseEntity<Object> response = genService.updateData("user", "pass", "entity", "1", updateData);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
