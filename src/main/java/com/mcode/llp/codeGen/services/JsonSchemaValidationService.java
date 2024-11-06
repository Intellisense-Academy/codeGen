package com.mcode.llp.codeGen.services;


import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

@Service
@Slf4j
public class JsonSchemaValidationService {
    private final String SCHEMA_VALIDATION_FILE = "E:\\codeGen\\src\\main\\resources\\validation.json";
    private JsonSchema jsonSchema;

    private void loadSchema() throws IOException {
        InputStream schemaInputStream = new FileInputStream(new File(SCHEMA_VALIDATION_FILE));
        jsonSchema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7).getSchema(schemaInputStream);
    }

    public String validateJson(JsonNode jsonNode) {
        try {
            loadSchema(); // Ensure schema is reloaded for every validation
            Set<ValidationMessage> errors = jsonSchema.validate(jsonNode);
            if (errors.isEmpty()) {
                log.info("event is valid");
                return "Valid";
            } else {
                log.info("event is invalid");
                return errors.toString();
            }
        } catch (IOException e) {
            log.error("Error loading schema: ", e);
            return "Schema loading error: " + e.getMessage();
        }
    }
}
