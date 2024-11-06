package com.mcode.llp.codeGen.models;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class SchemaFileUtil {
    public static final String SCHEMA_VALIDATION_FILE = "E:\\codeGen\\src\\main\\resources\\validation.json";

    public static void saveSchemaToFile(Schema schema) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(SCHEMA_VALIDATION_FILE), schema);
            System.out.println("Schema saved to validation.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
