package com.mcode.llp.codeGen.models;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
public class AppConfiguration {
    private static final String SCHEMA_VALIDATION_FILE = "E:\\codeGen\\src\\main\\resources\\validation.json";

    @Bean
    public JsonSchema jsonSchema() throws Exception {
            InputStream schemaInputStream = getClass().getResourceAsStream("/validation.json");
            if (schemaInputStream == null) {
                throw new IllegalArgumentException("Schema file not found!");
            }
            return JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7).getSchema(schemaInputStream);
        }

    }
