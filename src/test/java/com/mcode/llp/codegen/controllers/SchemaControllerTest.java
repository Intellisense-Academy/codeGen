package com.mcode.llp.codegen.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcode.llp.codegen.models.Schema;
import com.mcode.llp.codegen.services.SchemaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

@ExtendWith(MockitoExtension.class)
class SchemaControllerTest {

    @Mock
    private SchemaService schemaService;

    @Mock
    private HttpResponse<String> httpResponse;

    @InjectMocks
    private SchemaController schemaController;

    private Schema schema;

    @BeforeEach
    void setUp() {
        schema = new Schema();
        schema.setTitle("TestSchema");
    }

    @Test
    void testCreateSchema_Success() throws IOException, InterruptedException {
        when(httpResponse.body()).thenReturn("Schema Created Successfully");
        when(schemaService.insertSchema(anyString(), anyString(), any(Schema.class))).thenReturn(httpResponse);

        ResponseEntity<String> response = schemaController.createSchema(schema);

        assertEquals(CREATED, response.getStatusCode());
        assertEquals("Schema Created Successfully", response.getBody());
    }

    @Test
    void testCreateSchema_Exception() throws IOException, InterruptedException {
        when(schemaService.insertSchema(anyString(), anyString(), any(Schema.class))).thenThrow(new IOException("Error"));

        ResponseEntity<String> response = schemaController.createSchema(schema);

        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred", response.getBody());
    }

    @Test
    void testGetAllSchemas_Success() throws IOException, InterruptedException {
        JsonNode mockResponse = new ObjectMapper().createObjectNode();
        when(schemaService.getAllSchema()).thenReturn(mockResponse);

        ResponseEntity<JsonNode> response = schemaController.getAllSchemas();

        assertEquals(OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    void testGetAllSchemas_Exception() throws IOException, InterruptedException {
        when(schemaService.getAllSchema()).thenThrow(new IOException("Error"));

        ResponseEntity<JsonNode> response = schemaController.getAllSchemas();

        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetByName_Success() {
        JsonNode mockResponse = new ObjectMapper().createObjectNode();
        when(schemaService.getSchema("TestSchema")).thenReturn(mockResponse);

        ResponseEntity<JsonNode> response = schemaController.getByName("TestSchema");

        assertEquals(OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    void testGetByName_NotFound()  {
        when(schemaService.getSchema("TestSchema")).thenReturn(null);

        ResponseEntity<JsonNode> response = schemaController.getByName("TestSchema");

        assertEquals(NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUpdateSchema_Success() throws IOException, InterruptedException {
        when(httpResponse.body()).thenReturn("Schema Updated Successfully");
        when(schemaService.updateSchema(anyString(), any(Schema.class))).thenReturn(httpResponse);

        ResponseEntity<String> response = schemaController.updateSchema("TestSchema", schema);

        assertEquals(OK, response.getStatusCode());
        assertEquals("Schema Updated Successfully", response.getBody());
    }

    @Test
    void testUpdateSchema_Exception() throws IOException, InterruptedException {
        when(schemaService.updateSchema(anyString(), any(Schema.class))).thenThrow(new IOException("Error"));

        ResponseEntity<String> response = schemaController.updateSchema("TestSchema", schema);

        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred", response.getBody());
    }

    @Test
    void testDeleteSchema_Success() throws IOException, InterruptedException {
        when(httpResponse.body()).thenReturn("Schema Deleted Successfully");
        when(schemaService.deleteSchema("TestSchema")).thenReturn(httpResponse);

        ResponseEntity<String> response = schemaController.deleteSchema("TestSchema");

        assertEquals(OK, response.getStatusCode());
        assertEquals("Schema Deleted Successfully", response.getBody());
    }

    @Test
    void testDeleteSchema_Exception() throws IOException, InterruptedException {
        when(schemaService.deleteSchema("TestSchema")).thenThrow(new IOException("Error"));

        ResponseEntity<String> response = schemaController.deleteSchema("TestSchema");

        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred", response.getBody());
    }
}
