package com.mcode.llp.codegen.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.mcode.llp.codegen.models.SearchRequestPayload;
import com.mcode.llp.codegen.services.OpenSearchService;
import com.mcode.llp.codegen.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search")
public class SearchController {
    private final OpenSearchService opensearchService;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    private static final String ACTION = "An error {}";
    private static final String MESSAGE = "message";

    @Autowired
    public SearchController(OpenSearchService opensearchService, UserService userService){
        this.opensearchService=opensearchService;
        this.userService = userService;
    }

    @PostMapping("/query")
    public ResponseEntity<Object> search(@RequestBody SearchRequestPayload payload,@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        try {
            String[] credentials = userService.extractCredentials(authHeader);
            String username = credentials[0];
            String password = credentials[1];
            String entityName=payload.getMainQuery().getIndexName();
            ResponseEntity<Object> userValidResponse = userService.isValidUser(username, password,entityName,"GET");
            if (userValidResponse.getStatusCode() == HttpStatus.OK) {

                Object responseBody = userValidResponse.getBody();

                if (responseBody instanceof Map) {
                    Map<String, Object> bodyMap = (Map<String, Object>) responseBody;
                    if (bodyMap.containsKey("tenant")) {
                        String tenantName = bodyMap.get("tenant").toString();
                        List<String> fieldsToReturn = payload.getMainQuery().getFieldsToReturn();
                        JsonNode response = opensearchService.executeSearch(payload,tenantName,fieldsToReturn);
                        return ResponseEntity.ok(response);

                    }
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(MESSAGE, "unauthorized tenent"));

            }else{
                return userValidResponse;
            }

        } catch (IOException | InterruptedException  e) {
            logger.error(ACTION, e.getMessage());
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

}

