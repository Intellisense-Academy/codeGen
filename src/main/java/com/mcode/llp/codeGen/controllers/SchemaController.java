package com.mcode.llp.codeGen.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.mcode.llp.codeGen.models.Schema;
//import com.mcode.llp.codeGen.services.JsonSchemaValidationService;
import com.mcode.llp.codeGen.services.SchemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
    public class SchemaController {

        private static final Logger logger = LoggerFactory.getLogger(SchemaController.class);
        @Autowired
        private SchemaService openSearchService;
//        @Autowired
//        private JsonSchemaValidationService service;

        @PostMapping("/schemas")
        public ResponseEntity<?> createSchema(@RequestBody Schema schema) {
            try {
                String response = openSearchService.insertSchema("schemas", schema.getTitle(), schema);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                logger.error("Error occurred: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
            }
        }

        @GetMapping ("/schemas")
        public ResponseEntity<JsonNode> getAllSchemas(){
            try {
                JsonNode response = openSearchService.getAllSchema();
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(null);
            }
        }

        @GetMapping("/schemas/{entityName}")
        public ResponseEntity<JsonNode> getByName(@PathVariable(value = "entityName") String entityName) {
            try {
                JsonNode response = openSearchService.getSchema(entityName);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                logger.error("Error occurred: {}", e.getMessage());
                return ResponseEntity.internalServerError().body(null);
            }
        }

        @PutMapping("/schemas/{entityName}")
        public ResponseEntity<?> UpdateSchema(@PathVariable String entityName, @RequestBody Schema schemaData){
            try {
                String response = openSearchService.updateSchema(entityName,schemaData);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                logger.error("Error occurred: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
            }
        }

    @DeleteMapping("/schemas/{entityName}")
    public ResponseEntity<?> deleteSchema(@PathVariable(value = "entityName") String entityName) {
        try {
            String response = openSearchService.deleteSchema(entityName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }


//        @GetMapping("/schemas/{entityName}")
//        public ResponseEntity<Schema> getByName(@PathVariable(value = "entityName") String entityName) {
//            Schema schema = new Schema();
//            schema.setTitle(entityName);
//
//            Set<Property> properties = schemaService.getByName(entityName);
//            if (properties != null && !properties.isEmpty()) {
//                Map<String, Schema> schemaProperties = new HashMap<>();
//                Set<String> requiredFields = new HashSet<>();
//
//                for (Property property : properties) {
//                    Schema propertySchema = new Schema();
//                    schemaProperties.put(property.getName(), propertySchema);
//                    propertySchema.setType(property.getType());
//                    propertySchema.setMinimum(property.getMinimum());
//                    propertySchema.setMaximum(property.getMaximum());
//                    if (property.isRequired()) {
//                        requiredFields.add(property.getName());
//                    }
//                    schema.setProperties(schemaProperties);
//                    if (!requiredFields.isEmpty()) {
//                        schema.setRequired(requiredFields);
//                    }
//                }
//                return ResponseEntity.ok(schema);
//            } else {
//                return ResponseEntity.notFound().build();
//            }
//        }
//
//        @GetMapping("/schemas")
//        public ResponseEntity<List<Schema>> getAllEntities() {
//            List<String> entityNames = schemaService.getAllEntityNames();
//            if (entityNames == null || entityNames.isEmpty()) {
//                return ResponseEntity.noContent().build();
//            }
//
//            List<Schema> schemas = new ArrayList<>();
//
//            for (String entityName : entityNames) {
//                Schema schema = new Schema();
//                schema.setTitle(entityName);
//                Set<Property> properties = schemaService.getByName(entityName);
//
//                if (properties != null && !properties.isEmpty()) {
//                    Map<String, Schema> schemaProperties = new HashMap<>();
//                    Set<String> requiredFields = new HashSet<>();
//
//                    for (Property property : properties) {
//                        Schema propertySchema = new Schema();
//                        propertySchema.setType(property.getType());
//                        schemaProperties.put(property.getName(), propertySchema);
//                        propertySchema.setMinimum(property.getMinimum());
//                        propertySchema.setMaximum(property.getMaximum());
//
//                        if (property.isRequired()) {
//                            requiredFields.add(property.getName());
//                        }
//                    }
//                    schema.setProperties(schemaProperties);
//                    if (!requiredFields.isEmpty()) {
//
//                        schema.setRequired((requiredFields));
//
//                    }
//                }
//
//                schemas.add(schema);
//            }
//            return ResponseEntity.ok(schemas);
//        }
//
//
//
//            @PostMapping("/validate")
//            public boolean validateEvent( @RequestBody JsonNode jsonNode ) throws FileNotFoundException {
//                return service.validateJson(jsonNode,"student");
//            }
    }