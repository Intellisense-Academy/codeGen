package com.mcode.llp.codegen.controllers;
import com.mcode.llp.codegen.services.GenService;
import com.mcode.llp.codegen.services.JsonSchemaValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;

@RestController
public class GenController {
    HttpResponse<String> response ;
    private final JsonSchemaValidationService service;
    private final GenService genService;
    private static final Logger logger = LoggerFactory.getLogger(GenController.class);
    private final String ACTION_2 = "An error {}";
    @Autowired
    public GenController(JsonSchemaValidationService service, GenService genService) {
        this.service = service;
        this.genService=genService;
    }

    @PostMapping("/{entityName}")
    public ResponseEntity<String>  createEntity(@RequestBody Map<String, Object> requestBody, @PathVariable(value = "entityName") String entityName) {

        // Convert the requestBody to a JsonNode
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.valueToTree(requestBody);

        boolean isJsonExists = service.validateJson(jsonNode, entityName);
        String documentId = UUID.randomUUID().toString();
        try{
            if(isJsonExists){
                response=genService.insertData(entityName,documentId,jsonNode);
                return ResponseEntity.status(HttpStatus.CREATED).body(response.body());
            }else{
                return ResponseEntity.badRequest().build();
            }

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/{entityName}/{id}")
    public ResponseEntity<String> deleteEntity(@PathVariable("entityName") String entityName, @PathVariable("id") String id) {

        if (Boolean.TRUE.equals(genService.indexExists(entityName))) {
            if (id != null && !id.isEmpty()) {
                try {
                    response = genService.deleteData(entityName,id);
                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                } catch (IOException | InterruptedException e) {
                    logger.error(ACTION_2, e.getMessage());
                    Thread.currentThread().interrupt();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ACTION_2);
                }
            } else {
                return new ResponseEntity<>("ID is required to delete a record.", HttpStatus.BAD_REQUEST);
            }
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{entityName}/{id}")
    public ResponseEntity<JsonNode> viewDataById(@PathVariable("entityName") String entityName, @PathVariable("id") String id) {
        if(Boolean.TRUE.equals(genService.indexExists(entityName))){
            try {
                JsonNode responses = genService.getSingleData(entityName,id);
                if(responses == null){
                    return ResponseEntity.badRequest().build() ;
                }
                return ResponseEntity.ok(responses);
            } catch (Exception e) {
                logger.error(ACTION_2, e.getMessage());
                return ResponseEntity.internalServerError().body(null);
            }
        }else{
            return ResponseEntity.badRequest().build();
        }

    }

    @GetMapping("/{entityName}")
    public ResponseEntity<JsonNode> viewAllData(@PathVariable("entityName") String entityName) {
        if (Boolean.TRUE.equals(genService.indexExists(entityName))) {
            try {
                JsonNode responses = genService.getAllData(entityName);
                return ResponseEntity.ok(responses);
            } catch (Exception e) {
                logger.error(ACTION_2, e.getMessage());
                return ResponseEntity.internalServerError().body(null);
            }
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
    @PutMapping("/{entityName}/{id}")
    public ResponseEntity<String> updateEntity(@PathVariable String entityName, @PathVariable String id, @RequestBody Map<String, Object> updateData) {
        if (Boolean.TRUE.equals(genService.indexExists(entityName))) {
            try {
                response = genService.updateData(entityName,id,updateData);
                return ResponseEntity.ok(response.body());
            } catch (IOException | InterruptedException e) {
                logger.error(ACTION_2, e.getMessage());
                Thread.currentThread().interrupt();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ACTION_2);
            }
        }else{
            return ResponseEntity.badRequest().build();
        }
    }
}
