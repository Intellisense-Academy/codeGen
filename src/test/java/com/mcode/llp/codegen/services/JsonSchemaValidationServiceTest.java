package com.mcode.llp.codegen.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JsonSchemaValidationServiceTest {

    @Mock
    private SchemaService schemaService;

    @InjectMocks
    private JsonSchemaValidationService validationService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private JsonNode validJson;
    private JsonNode invalidJson;
    private JsonNode schemaJson;

    @BeforeEach
    void setUp() throws IOException {
        String schemaStr = """
                {
                    "$schema": "http://json-schema.org/draft-07/schema#",
                    "type": "object",
                    "properties": {
                        "name": { "type": "string" },
                        "age": { "type": "integer" }
                    },
                    "required": ["name", "age"]
                }
                """;
        schemaJson = objectMapper.readTree(schemaStr);

        String validJsonStr = """
                {
                    "name": "John Doe",
                    "age": 30
                }
                """;
        validJson = objectMapper.readTree(validJsonStr);

        String invalidJsonStr = """
                {
                    "name": "John Doe"
                }
                """;
        invalidJson = objectMapper.readTree(invalidJsonStr);

        when(schemaService.getSchema("testEntity")).thenReturn(schemaJson);
    }

    @Test
    void testValidJson() {
        Set<ValidationMessage> errors = validationService.validateJson(validJson, "testEntity");
        assertTrue(errors.isEmpty(), "Expected no validation errors for valid JSON");
    }

    @Test
    void testInvalidJson() {
        Set<ValidationMessage> errors = validationService.validateJson(invalidJson, "testEntity");
        assertFalse(errors.isEmpty(), "Expected validation errors for invalid JSON");
    }

    @Test
    void testSchemaLoadFailure(){
        when(schemaService.getSchema("testEntity")).thenReturn(null);
        Set<ValidationMessage> errors = validationService.validateJson(validJson, "testEntity");
        assertFalse(errors.isEmpty(), "Expected schema load failure error");
        assertEquals("SCHEMA_LOAD_ERROR", errors.iterator().next().getCode(), "Expected SCHEMA_LOAD_ERROR code");
    }

}
