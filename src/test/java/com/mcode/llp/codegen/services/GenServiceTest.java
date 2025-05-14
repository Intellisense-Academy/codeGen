package com.mcode.llp.codegen.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcode.llp.codegen.databases.OpenSearchClient;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.http.HttpResponse;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GenServiceTest {

    @InjectMocks
    private GenService genService;

    @Mock
    private OpenSearchClient openSearchClient;

    @Mock
    private UserService userService;

    @Mock
    private JsonSchemaValidationService schemaService;

    @Mock
    private HttpResponse<String> httpResponse;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testIndexExists_whenExists() throws Exception {
        when(openSearchClient.sendRequest("/testEntity", "HEAD", null)).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        assertTrue(genService.indexExists("testEntity"));
    }

    @Test
    void testIndexExists_whenNotExists() throws Exception {
        when(openSearchClient.sendRequest("/testEntity", "HEAD", null)).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(404);
        assertFalse(genService.indexExists("testEntity"));
    }

    @Test
    void testInsertData_valid() throws Exception {
        ObjectNode data = objectMapper.createObjectNode();
        data.put("name", "Test");

        ResponseEntity<Object> userValidResponse = new ResponseEntity<>(Map.of("tenant", "abc"), HttpStatus.OK);

        when(userService.isValidUser("user", "pass", "schema", "POST")).thenReturn(userValidResponse);
        when(schemaService.validateJson(data, "schema")).thenReturn(Collections.emptySet());
        when(openSearchClient.sendRequest(anyString(), eq("POST"), anyString())).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn("created");

        ResponseEntity<Object> response = genService.insertData("user", "pass", "schema", data);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void testInsertData_invalidJson() throws Exception {
        ObjectNode data = objectMapper.createObjectNode();
        data.put("name", "Test");

        Set<ValidationMessage> errors = Set.of(new ValidationMessage.Builder().message("invalid field").build());
        ResponseEntity<Object> userValidResponse = new ResponseEntity<>(Map.of("tenant", "abc"), HttpStatus.OK);

        when(userService.isValidUser(any(), any(), any(), any())).thenReturn(userValidResponse);
        when(schemaService.validateJson(any(), any())).thenReturn(errors);

        ResponseEntity<Object> response = genService.insertData("user", "pass", "schema", data);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testInsertData_invalidUser() throws Exception {
        when(userService.isValidUser(any(), any(), any(), any()))
                .thenReturn(new ResponseEntity<>("unauthorized", HttpStatus.UNAUTHORIZED));
        ResponseEntity<Object> response = genService.insertData("u", "p", "s", objectMapper.createObjectNode());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testDeleteData_valid() throws Exception {
        ResponseEntity<Object> userValidResponse = new ResponseEntity<>(HttpStatus.OK);
        when(userService.isValidUser(any(), any(), any(), any())).thenReturn(userValidResponse);
        when(openSearchClient.sendRequest(anyString(), eq("DELETE"), isNull())).thenReturn(httpResponse);

        ResponseEntity<Object> response = genService.deleteData("user", "pass", "entity", "123");
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testGetSingleData_valid() throws Exception {
        ObjectNode source = objectMapper.createObjectNode();
        source.put("id", "1");
        ObjectNode root = objectMapper.createObjectNode();
        root.set("_source", source);

        when(userService.isValidUser(any(), any(), any(), any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        when(openSearchClient.sendRequest(anyString(), eq("GET"), isNull())).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn(root.toString());

        ResponseEntity<Object> response = genService.getSingleData("u", "p", "e", "1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetAllData_valid() throws Exception {
        ObjectNode source = objectMapper.createObjectNode();
        source.put("field", "value");

        ObjectNode hit = objectMapper.createObjectNode();
        hit.set("_source", source);

        ObjectNode hits = objectMapper.createObjectNode();
        hits.set("hits", objectMapper.createArrayNode().add(hit));

        ObjectNode responseJson = objectMapper.createObjectNode();
        responseJson.set("hits", hits);

        when(userService.isValidUser(any(), any(), any(), any()))
                .thenReturn(new ResponseEntity<>(Map.of("tenant", "abc"), HttpStatus.OK));
        when(openSearchClient.sendRequest(anyString(), eq("GET"), isNull())).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn(responseJson.toString());

        ResponseEntity<Object> response = genService.getAllData("u", "p", "entity");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdateData_valid() throws Exception {
        Map<String, Object> updateData = Map.of("name", "newName");

        when(userService.isValidUser(any(), any(), any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));
        when(openSearchClient.sendRequest(anyString(), eq("POST"), anyString()))
                .thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn("{\"result\":\"updated\"}");

        ResponseEntity<Object> response = genService.updateData("u", "p", "entity", "id123", updateData);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdateData_invalidUser() throws Exception {
        when(userService.isValidUser(any(), any(), any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.FORBIDDEN));
        ResponseEntity<Object> response = genService.updateData("u", "p", "entity", "id", Map.of());
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

}
