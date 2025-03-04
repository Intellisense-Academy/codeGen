package com.mcode.llp.codegen.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.mcode.llp.codegen.models.Schema;
import com.mcode.llp.codegen.services.SchemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.http.HttpResponse;

@RestController
    public class SchemaController {

        private static final Logger logger = LoggerFactory.getLogger(SchemaController.class);
        private static final String ACTION_1 = "An error occurred";
        private static final String ACTION_2 = "Error occurred: {}";
        HttpResponse<String> response ;
        private final SchemaService openSearchService;

        @Autowired
        public SchemaController(SchemaService openSearchService) {
            this.openSearchService = openSearchService;
        }

        @PostMapping("/schemas")
        public ResponseEntity<String> createSchema(@RequestBody Schema schema) {
            try {
                response = openSearchService.insertSchema("schemas", schema.getTitle(), schema);
                return ResponseEntity.status(HttpStatus.CREATED).body(response.body());
            } catch (IOException | InterruptedException e) {
                logger.error(ACTION_2, e.getMessage());
                Thread.currentThread().interrupt();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ACTION_1);
            }
        }

        @GetMapping ("/schemas")
        public ResponseEntity<JsonNode> getAllSchemas(){
            try {
                JsonNode responses = openSearchService.getAllSchema();
                return ResponseEntity.ok(responses);
            } catch (IOException | InterruptedException e) {
                logger.error(ACTION_2, e.getMessage());
                Thread.currentThread().interrupt();
                return ResponseEntity.internalServerError().body(null);
            }
        }

        @GetMapping("/schemas/{entityName}")
        public ResponseEntity<JsonNode> getByName(@PathVariable(value = "entityName") String entityName) {
            try {
                JsonNode responses = openSearchService.getSchema(entityName);
                if(responses == null){
                    return ResponseEntity.notFound().build() ;
                }
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
                return ResponseEntity.ok(response.body());
            } catch (IOException | InterruptedException e) {
                logger.error(ACTION_2, e.getMessage());
                Thread.currentThread().interrupt();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ACTION_1);
            }
        }

    @DeleteMapping("/schemas/{entityName}")
    public ResponseEntity<String> deleteSchema(@PathVariable(value = "entityName") String entityName) {
        try {
            response = openSearchService.deleteSchema(entityName);
            return ResponseEntity.ok(response.body());
        } catch (IOException | InterruptedException e) {
            logger.error(ACTION_2, e.getMessage());
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ACTION_1);
        }
    }
    }