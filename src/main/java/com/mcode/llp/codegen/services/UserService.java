package com.mcode.llp.codegen.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcode.llp.codegen.databases.OpenSearchClient;
import com.mcode.llp.codegen.notification.WhatAppNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserService {

    HttpResponse<String> response ;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String ERROR = "An error {}";
    private static final String MESSAGE = "message";
    private static final String EMAIL = "email";
    private static final String TENENT = "tenant";
    private final OpenSearchClient client;
    private final WhatAppNotification notification;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public  UserService(OpenSearchClient client, WhatAppNotification notification){
        this.client = client;
        this.notification=notification;
    }

    public String[] extractCredentials(String authHeader){
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }

        try {
            // Extract credentials from Basic Auth
            String base64Credentials = authHeader.substring("Basic ".length());
            byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(decodedBytes);
            String[] values = credentials.split(":", 2); // Split into two parts only

            if (values.length < 2) {
                throw new IllegalArgumentException("Invalid Basic Authentication format");
            }

            return values; // Returns ["email", "password"]

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to decode authorization header");
        }
    }

    private JsonNode getUserByemail(String email) throws IOException, InterruptedException {
        String endpoint = "/users/_search?q=email:" + email;
        response = client.sendRequest(endpoint, "GET", null);
        JsonNode jsonNode = objectMapper.readTree(response.body());
        JsonNode hits = jsonNode.path("hits").path("hits");
        return hits.isEmpty() ? null : hits.get(0).path("_source");
    }

    public ResponseEntity<Object> loginUser(String email, String password) throws IOException, InterruptedException {
        if (email.isEmpty() || password.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(MESSAGE, "email and password must be provided."));
        }

        try {
            JsonNode userData = getUserByemail(email);

            if (userData == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(MESSAGE, "No user found"));
            }


            String storedemail = userData.path(EMAIL).asText();
            String storedPassword = userData.path("password").asText();

            if (!storedemail.equals(email) || !storedPassword.equals(password)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(MESSAGE, "Invalid email or Password."));
            }

            String role = userData.path("role").asText();
            String tenant = userData.path(TENENT).asText();

            return ResponseEntity.ok(Map.of(
                    EMAIL, storedemail,
                    "password",storedPassword,
                    "role", role,
                    TENENT, tenant
            ));

        } catch (IOException | InterruptedException e) {
            logger.error(ERROR, e.getMessage());
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(MESSAGE, "Internal server error."));
        }
    }

    public ResponseEntity<Object> isValidUser(String email, String password, String entityName, String operation) throws IOException, InterruptedException {
        if (email.isEmpty() || password.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(MESSAGE, "Authentication required. Please provide a email and password."));
        }
        String endpoint = "/users/_search?q=username:" + email;
        try {
            JsonNode userData = getUserByemail(email);

            // Parse JSON response
            JsonNode jsonNode = objectMapper.readTree(response.body());

            // Extract hits array
            JsonNode hits = jsonNode.path("hits").path("hits");
            if (hits.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(MESSAGE, "No user found"));
            }

            // Extract user details
            JsonNode userData = hits.get(0).path("_source");
            if (userData.isMissingNode()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(MESSAGE, "User data not found"));
            }

            // Get user details
            String storedemail = userData.path(EMAIL).asText();
            String storedPassword = userData.path("password").asText();
            String storedRole = userData.path("role").asText();
            String storedTenet = userData.path("tenant").asText();

            // Validate email and password
            if (!storedemail.equals(email) || !storedPassword.equals(password)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(MESSAGE, "Invalid email or password"));
            }

            boolean responseData = isAuthorizedUser(entityName,storedRole,operation);
            if(responseData){
                return ResponseEntity.ok(Map.of("tenant", storedTenet));
            }else{
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(MESSAGE, "Your are not permitted to access"));
            }
        } catch (IOException | InterruptedException e) {
            logger.error(ERROR, e.getMessage());
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(MESSAGE, "Internal Server Error"));
        }
    }

    private JsonNode getPermissionData(String entityName) throws IOException, InterruptedException{
        String endpoint = "/settings/_search?q=entity:" + entityName;

        response = client.sendRequest(endpoint, "GET", null);
        JsonNode jsonNode = objectMapper.readTree(response.body());

        JsonNode hits = jsonNode.path("hits").path("hits");
        if (hits.isEmpty()) {
            return null;
        }

        JsonNode sourceNode = hits.get(0).path("_source");
        if (sourceNode.isMissingNode()) {
            return null;
        }
        return sourceNode;
    }

    private boolean containsValue(JsonNode arrayNode, String value) {
        for (JsonNode node : arrayNode) {
            if (node.asText().equals(value)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAuthorizedUser(String entityName, String role, String operation) throws IOException, InterruptedException {
        JsonNode permissionData = getPermissionData(entityName);

        if(permissionData==null){
            return false;
        }

        // roles is now an array
        JsonNode rolesArrayNode = permissionData.path("roles");

        if (!rolesArrayNode.isArray()) {
            return false;
        }

        for (JsonNode roleObj : rolesArrayNode) {
            JsonNode allowedRolesNode = roleObj.path("allowedRoles");
            JsonNode operationsNode = roleObj.path("operations");

            boolean roleExists = allowedRolesNode.isArray() && containsValue(allowedRolesNode, role);
            boolean operationExists = operationsNode.isArray() && containsValue(operationsNode, operation);

            if (roleExists && operationExists) {
                return true; // If any object matches, user is authorized
            }
        }

        return false;
    }

    public boolean getNotificationDetails(String entityName, String operation, String responseBody) throws IOException,InterruptedException {
        JsonNode permissionData = getPermissionData(entityName);
        if (permissionData == null) return false;

        JsonNode notificationsNode = permissionData.path("notifications");
        JsonNode operationsNode = notificationsNode.path("operations");

        boolean isEnabled = notificationsNode.path("enabled").asBoolean(false);
        boolean operationExists = operationsNode.isArray() && containsValue(operationsNode, operation);

        if (isEnabled && operationExists){

            // Step 1: Parse response body
            JsonNode inputNode = objectMapper.readTree(responseBody);

            // Step 2: get the content and to from notification node
            String content = notificationsNode.path("content").asText("");
            String to = notificationsNode.path("to").asText("");
            List<String> keys = extractPlaceholderKeys(content);

            JsonNode contributorNode = getConnectedNodeIfKeyExists(keys,inputNode);

            String rootKey = getConnectingKey(content);

            String finalMessage = replaceAllPlaceholders(content, inputNode, contributorNode, rootKey);
            String finalPhoneNumber = replaceAllPlaceholders(to, inputNode, contributorNode, rootKey);

            return notification.sendWhatsAppMessage(finalPhoneNumber, finalMessage);
        }else{
            return false;
        }
    }

    public JsonNode getDocumentById(String indexName, String id) throws IOException, InterruptedException {
        String endpoint = "/" + indexName + "/_doc/" + id;
        HttpResponse<String> getResponse = client.sendRequest(endpoint, "GET", null);

        if (getResponse.statusCode() == 200) {
            String responseBody = getResponse.body();

            JsonNode fullJson = new ObjectMapper().readTree(responseBody);
            return fullJson.path("_source");
        }
        return null;
    }

    public List<String> extractPlaceholderKeys(String template) {
        Pattern pattern = Pattern.compile("\\$\\{(.*?)}");
        Matcher matcher = pattern.matcher(template);
        List<String> keys = new ArrayList<>();

        while (matcher.find()) {
            keys.add(matcher.group(1));
        }
        return keys;
    }

    public JsonNode getConnectedNodeIfKeyExists(List<String> placeholderKeys, JsonNode inputNode) throws IOException, InterruptedException {
        String id;
        for (String key : placeholderKeys) {
            if (key.contains(".")) {
                String rootKey = key.substring(0, key.indexOf("."));

                Iterator<String> fieldNames = inputNode.fieldNames();
                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    if (fieldName.toLowerCase().startsWith(rootKey.toLowerCase())) {
                        id = inputNode.path(fieldName).asText();
                        return getDocumentById(rootKey,id);
                    }
                }
            }
        }
        return null;
    }

    public String getConnectingKey(String template) {
        Pattern pattern = Pattern.compile("\\$\\{(\\w+)\\.");
        Matcher matcher = pattern.matcher(template);

        if (matcher.find()) {
            return matcher.group(1); // e.g., "contributor" from "${contributor.name}"
        }

        return null;
    }

    public String replaceAllPlaceholders(String template, JsonNode inputNode, JsonNode connectedNode, String rootKey) {
        List<String> keys = extractPlaceholderKeys(template);

        for (String key : keys) {
            String value = "";

            // Handle dot notation (e.g., contributor.name)
            if (key.contains(".")) {
                String[] parts = key.split("\\.");
                if (parts.length == 2 && parts[0].equals(rootKey) && connectedNode != null) {
                    value = connectedNode.path(parts[1]).asText("");
                }
            } else {
                // Fallback to inputNode (e.g., amount, tenant)
                value = inputNode.path(key).asText("");
            }

            template = template.replace("${" + key + "}", value);
        }

        return template;
    }
}