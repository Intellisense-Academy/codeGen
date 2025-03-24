package com.mcode.llp.codegen.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcode.llp.codegen.services.GenService;
import com.mcode.llp.codegen.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenControllerTest {

    @Mock
    private GenService genService;

    @Mock
    private UserService userService;

    @InjectMocks
    private GenController genController;

    private static final String AUTH_HEADER = "Basic dXNlcjpwYXNzd29yZA==";
    private static final String ENTITY_NAME = "testEntity";
    private static final String ID = "123";

    @BeforeEach
    void setUp() {
        when(userService.extractCredentials(AUTH_HEADER)).thenReturn(new String[]{"user", "password"});
    }

    @Test
    void testCreateEntity() throws IOException, InterruptedException {
        JsonNode requestBody = new ObjectMapper().createObjectNode();
        ResponseEntity<Object> mockResponse = ResponseEntity.ok("Created");
        when(genService.insertData("user", "password", ENTITY_NAME, requestBody)).thenReturn(mockResponse);

        ResponseEntity<Object> response = genController.createEntity(AUTH_HEADER, requestBody, ENTITY_NAME);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Created", response.getBody());
    }

    @Test
    void testDeleteEntity() throws IOException, InterruptedException {
        when(genService.indexExists(ENTITY_NAME)).thenReturn(true);
        ResponseEntity<Object> mockResponse = ResponseEntity.ok("Deleted");
        when(genService.deleteData("user", "password", ENTITY_NAME, ID)).thenReturn(mockResponse);

        ResponseEntity<Object> response = genController.deleteEntity(AUTH_HEADER, ENTITY_NAME, ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Deleted", response.getBody());
    }

    @Test
    void testViewDataById_NotFound() {
        when(genService.indexExists(ENTITY_NAME)).thenReturn(true);
        when(genService.getSingleData("user", "password", ENTITY_NAME, ID)).thenReturn(null);

        ResponseEntity<Object> response = genController.viewDataById(AUTH_HEADER, ENTITY_NAME, ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testViewAllData() throws IOException, InterruptedException {
        when(genService.indexExists(ENTITY_NAME)).thenReturn(true);
        ResponseEntity<Object> mockResponse = ResponseEntity.ok("Data List");
        when(genService.getAllData("user", "password", ENTITY_NAME)).thenReturn(mockResponse);

        ResponseEntity<Object> response = genController.viewAllData(AUTH_HEADER, ENTITY_NAME);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Data List", response.getBody());
    }

    @Test
    void testUpdateEntity() throws IOException, InterruptedException {
        when(genService.indexExists(ENTITY_NAME)).thenReturn(true);
        ResponseEntity<Object> mockResponse = ResponseEntity.ok(Map.of("status", "updated"));
        when(genService.updateData("user", "password", ENTITY_NAME, ID, Map.of())).thenReturn(mockResponse);

        ResponseEntity<Object> response = genController.updateEntity(AUTH_HEADER, ENTITY_NAME, ID, Map.of());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("updated"));
    }
}
