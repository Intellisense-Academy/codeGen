package com.mcode.llp.codegen.controllers;
import com.mcode.llp.codegen.services.GenService;
import com.mcode.llp.codegen.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.*;

@RestController
public class GenController {
    HttpResponse<String> response ;
    private final UserService userService;
    private final GenService genService;
    private static final Logger logger = LoggerFactory.getLogger(GenController.class);
    private static final String ACTION2 = "An error {}";
    @Autowired
    public GenController(GenService genService,UserService userService) {
        this.genService=genService;
        this.userService = userService;
    }

    @PostMapping("/{entityName}")
    public ResponseEntity<Object> createEntity (@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @RequestBody JsonNode  requestBody, @PathVariable(value = "entityName") String entityName) {

        try{
            String[] credentials = userService.extractCredentials(authHeader);
            String username = credentials[0];
            String password = credentials[1];
            return genService.insertData(username,password,entityName,requestBody);
        }catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/{entityName}/{id}")
    public ResponseEntity<String> deleteEntity(@PathVariable("entityName") String entityName, @PathVariable("id") String id) {

        if (genService.indexExists(entityName)) {
                try {
                    genService.deleteData(entityName,id);
                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                } catch (IOException | InterruptedException e) {
                    logger.error(ACTION2, e.getMessage());
                    Thread.currentThread().interrupt();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ACTION2);
                }
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{entityName}/{id}")
    public ResponseEntity<JsonNode> viewDataById(@PathVariable("entityName") String entityName, @PathVariable("id") String id) {
        if(genService.indexExists(entityName)){
            try {
                JsonNode responses = genService.getSingleData(entityName,id);
                if(responses == null){
                    return ResponseEntity.notFound().build();
                }
                return ResponseEntity.ok(responses);
            } catch (Exception e) {
                logger.error(ACTION2, e.getMessage());
                return ResponseEntity.internalServerError().body(null);
            }
        }else{
            return ResponseEntity.badRequest().build();
        }

    }

    @GetMapping("/{entityName}")
    public ResponseEntity<List<JsonNode>> viewAllData(@PathVariable("entityName") String entityName) {
        if (genService.indexExists(entityName)) {
            try {
                List<JsonNode> responses = genService.getAllData(entityName);
                return ResponseEntity.ok(responses);
            } catch (IOException | InterruptedException e) {
                logger.error(ACTION2, e.getMessage());
                Thread.currentThread().interrupt();
                return ResponseEntity.internalServerError().body(null);
            }
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
    @PutMapping("/{entityName}/{id}")
    public ResponseEntity<String> updateEntity(@PathVariable String entityName, @PathVariable String id, @RequestBody Map<String, Object> updateData) {
        if (genService.indexExists(entityName)) {
            try {
                response = genService.updateData(entityName,id,updateData);
                return ResponseEntity.ok(response.body());
            } catch (IOException | InterruptedException e) {
                logger.error(ACTION2, e.getMessage());
                Thread.currentThread().interrupt();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ACTION2);
            }
        }else{
            return ResponseEntity.badRequest().build();
        }
    }
}
