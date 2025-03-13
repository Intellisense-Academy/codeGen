package com.mcode.llp.codegen.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcode.llp.codegen.databases.OpenSearchClient;
import com.mcode.llp.codegen.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;

@Service
public class UserService {

    HttpResponse<String> response ;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String ERROR = "An error {}";
    private final OpenSearchClient client;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public  UserService(OpenSearchClient client){
        this.client = client;
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

            return values; // Returns ["username", "password"]

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to decode authorization header");
        }
    }

    public ResponseEntity<Object> isValidUser(String username, String password, String entityName) throws IOException, InterruptedException {
        if (username.isEmpty() || password.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required. Please provide a username and password."));
        }
        String endpoint = "/users/_search?q=username:" + username;
        try {
            response = client.sendRequest(endpoint, "GET", null);

            // Parse JSON response
            JsonNode jsonNode = objectMapper.readTree(response.body());

            // Extract hits array
            JsonNode hits = jsonNode.path("hits").path("hits");
            if (hits.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No user found"));
            }

            // Extract user details
            JsonNode userData = hits.get(0).path("_source");
            if (userData.isMissingNode()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "User data not found"));
            }

            // Get user details
            String storedUsername = userData.path("username").asText();
            String storedPassword = userData.path("password").asText();
            String storedRole = userData.path("role").asText();
            String storedTenet = userData.path("tenant").asText();

            // Validate username and password
            if (!storedUsername.equals(username) || !storedPassword.equals(password)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid username or password"));
            }

            if(entityName.equals("users") && !storedRole.equals("superuser")){
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message","no permission to access the superAdmin"));
            }


            // If everything is correct, return 200 OK with tenant
            return ResponseEntity.ok(Map.of("tenant", storedTenet));

        } catch (IOException | InterruptedException e) {
            logger.error(ERROR, e.getMessage());
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal Server Error"));
        }
    }

}
