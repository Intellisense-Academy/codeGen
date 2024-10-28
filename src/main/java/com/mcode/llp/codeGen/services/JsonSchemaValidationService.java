package com.mcode.llp.codeGen.services;


import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
public class JsonSchemaValidationService {
    @Autowired
    private JsonSchema jsonSchema;

    public String validateJson(JsonNode jsonNode) {

        Set<ValidationMessage> errors = jsonSchema.validate(jsonNode);
        //if errors have a single miss match, there would be a value in the errors set.
        if (errors.isEmpty()) {
            //event is valid.
            log.info("event is valid");
        } else {
            //event is in_valid.
            log.info("event is invalid");
        }
        return errors.toString();

    }
}