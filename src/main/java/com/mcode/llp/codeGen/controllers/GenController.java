package com.mcode.llp.codegen.controllers;
import com.mcode.llp.codegen.managers.QueryManager;
import com.mcode.llp.codegen.services.JsonSchemaValidationService;
import com.mcode.llp.codegen.validators.GenValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@RestController
public class GenController {

    @Autowired
    private JsonSchemaValidationService service;

    private final GenValidator genValidator;
    private final QueryManager queryManager;

    @Autowired
    public GenController(QueryManager queryManager, GenValidator genValidator) {
        this.queryManager = queryManager;
        this.genValidator = genValidator;
    }

    @PostMapping("/{entityName}")
    public ResponseEntity<Map<String, Object>> createEntity(
            @RequestBody Map<String, Object> requestBody,
            @PathVariable(value = "entityName") String entityName) {

        // Convert the requestBody to a JsonNode
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.valueToTree(requestBody);

        boolean isEntityExists = genValidator.isEntityExists(entityName);
        boolean isJsonExists = service.validateJson(jsonNode, entityName);

        if (isEntityExists && isJsonExists) {
            //queryManager.createTable(entityName);
            queryManager.insertTable(entityName, requestBody);
            return new ResponseEntity<>(requestBody, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{entityName}/{id}")
    public ResponseEntity<?> deleteEntity(
            @PathVariable("entityName") String entityName,
            @PathVariable("id") String id) {

        boolean isEntityExists = genValidator.isEntityExists(entityName);

        if (isEntityExists) {
            if (id != null && !id.isEmpty()) {
                queryManager.deleteTable(entityName, id);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>("ID is required to delete a record.", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{entityName}/{id}")
    public ResponseEntity<?> viewDataById(
            @PathVariable("entityName") String entityName, @PathVariable("id") String id) {

        boolean isEntityExists = genValidator.isEntityExists(entityName);

        if (isEntityExists) {
            Map<String, Object> data = queryManager.viewDataById(entityName, id);
            if (data.isEmpty()) {
                return new ResponseEntity<>("No data found with the given ID.", HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>(data, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{entityName}")
    public ResponseEntity<?> viewAllData(@PathVariable("entityName") String entityName) {
        boolean isEntityExists = genValidator.isEntityExists(entityName);

        if (isEntityExists) {
            List<Map<String, Object>> data = queryManager.viewAllData(entityName);
            return new ResponseEntity<>(data, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
//    @PutMapping("/{entityName}/{id}")
//    public void updateEntity(@PathVariable String entityName, @PathVariable String id, @RequestBody Map<String, Object> updates) {
//        queryManager.updateTable(entityName, id , updates);
//    }
}
