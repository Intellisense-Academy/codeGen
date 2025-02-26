package com.mcode.llp.codegen.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Set;

@Service
@Slf4j
public class JsonSchemaValidationService {

    private final SchemaService schemaService;

    @Autowired
    public JsonSchemaValidationService(SchemaService schemaService){
        this.schemaService=schemaService;
    }

    private JsonSchema jsonSchema;

    /**
     * Load schema dynamically from database instead of hardcoding.
     */
    private void loadSchema(String entityName) throws IOException {
        JsonNode schemaJson = schemaService.getSchema(entityName);
        if (schemaJson == null) {
            throw new IOException("Schema not found for entity: " + entityName);
        }

        jsonSchema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7).getSchema(schemaJson.toString());
    }

    /**
     * Validate JSON against dynamically loaded schema.
     */
    public boolean validateJson(JsonNode jsonNode, String entityName) {
        try {
            loadSchema(entityName); // Load schema dynamically

            Set<ValidationMessage> errors = jsonSchema.validate(jsonNode);
            if (errors.isEmpty()) {
                log.info("JSON is valid for entity: " + entityName);
                return true;
            } else {
                log.info("JSON is invalid for entity: " + entityName);
                return false;
            }
        } catch (IOException e) {
            log.error("Error loading schema: ", e.getMessage());
            return false;
        }
    }
}
