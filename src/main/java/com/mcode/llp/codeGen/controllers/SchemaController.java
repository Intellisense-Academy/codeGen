package com.mcode.llp.codegen.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.mcode.llp.codegen.models.Schema;
import com.mcode.llp.codegen.services.SchemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
    public class SchemaController {

        private static final Logger logger = LoggerFactory.getLogger(SchemaController.class);
        private static final String ACTION_1 = "An error occurred";
        private static final String ACTION_2 = "Error occurred: {}";
        String response ;
        private final SchemaService openSearchService;

        public SchemaController(SchemaService openSearchService) {
            this.openSearchService = openSearchService;
        }

        @PostMapping("/schemas")
        public ResponseEntity<String> createSchema(@RequestBody Schema schema) {
            try {
                response = openSearchService.insertSchema("schemas", schema.getTitle(), schema);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } catch (Exception e) {
                logger.error(ACTION_2, e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ACTION_1);
            }
        }

        @GetMapping ("/schemas")
        public ResponseEntity<JsonNode> getAllSchemas(){
            try {
                JsonNode responses = openSearchService.getAllSchema();
                return ResponseEntity.ok(responses);
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(null);
            }
        }

        @GetMapping("/schemas/{entityName}")
        public ResponseEntity<JsonNode> getByName(@PathVariable(value = "entityName") String entityName) {
            try {
                JsonNode responses = openSearchService.getSchema(entityName);
                return ResponseEntity.ok(responses);
            } catch (Exception e) {
                logger.error(ACTION_2, e.getMessage());
                return ResponseEntity.internalServerError().body(null);
            }
        }

        @PutMapping("/schemas/{entityName}")
        public ResponseEntity<String> updateSchema(@PathVariable String entityName, @RequestBody Schema schemaData){
            try {
                response = openSearchService.updateSchema(entityName,schemaData);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                logger.error(ACTION_2, e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ACTION_1);
            }
        }

    @DeleteMapping("/schemas/{entityName}")
    public ResponseEntity<String> deleteSchema(@PathVariable(value = "entityName") String entityName) {
        try {
            response = openSearchService.deleteSchema(entityName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error(ACTION_2, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ACTION_1);
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