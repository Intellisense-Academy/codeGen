package com.mcode.llp.codegen.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcode.llp.codegen.databases.OpenSearchClient;
import com.networknt.schema.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class GenService {
    private final OpenSearchClient openSearchClient;
    private final JsonSchemaValidationService service;
    private final UserService userService;
    HttpResponse<String> response ;
    private static final String TENENT = "tenant";
    private static final String SOURCE = "_source";
    private static final String DOC = "/_doc/";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(GenService.class);
    private static final String ERROR = "An error {}";

    @Autowired
    public GenService(OpenSearchClient openSearchClient,UserService userService,JsonSchemaValidationService service){
        this.openSearchClient=openSearchClient;
        this.userService = userService;
        this.service = service;
    }

    public boolean indexExists(String entityName){
        try{
            response = openSearchClient.sendRequest("/"+entityName ,"HEAD", null);
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            logger.error(e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    public Map<String, Object> addTencent(Object responseBody, JsonNode data){
        Map<String, Object> responseData = objectMapper.convertValue(responseBody, new TypeReference<HashMap<String, Object>>() {});
        String tenant = (String) responseData.get(TENENT);
        Map<String, Object> requestData = objectMapper.convertValue(data,new TypeReference<HashMap<String, Object>>() {});
        if (tenant != null) {
            requestData.put(TENENT, tenant);
        }
        return requestData;
    }

    public ResponseEntity<Object> insertData(String username, String password, String schemaName, JsonNode data) throws IOException,InterruptedException {
        try{
            ResponseEntity<Object> userValidResponse = userService.isValidUser(username, password,schemaName,"POST");
            if (userValidResponse.getStatusCode() == HttpStatus.OK) {
                Set<String> messages = new HashSet<>();
                for (ValidationMessage msg : service.validateJson(data, schemaName)) {
                    messages.add(msg.getMessage()); // Extracting only the message
                }if(messages.isEmpty()){
                    String requestBody;
                    if(data.has(TENENT)){
                        requestBody=objectMapper.writeValueAsString(data);
                    }else{
                        requestBody=objectMapper.writeValueAsString(addTencent(userValidResponse.getBody(),data));
                    }
                    String documentId = UUID.randomUUID().toString();
                    String endpoint = "/" + schemaName + DOC + documentId;
                    response= openSearchClient.sendRequest(endpoint, "POST", requestBody);
                    return ResponseEntity.status(HttpStatus.CREATED).body(response.body());
                }else{
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("message", "Validation failed");
                    errorResponse.put("errors", messages);
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            }else{
                    return userValidResponse;
            }
        }catch (IOException | InterruptedException e){
            logger.error(ERROR, e.getMessage());
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    public ResponseEntity<Object> deleteData(String username, String password, String entityName,String documentId) throws IOException,InterruptedException {
        try{
            ResponseEntity<Object> userValidResponse = userService.isValidUser(username, password,entityName,"DELETE");
            if (userValidResponse.getStatusCode() == HttpStatus.OK) {
                String endpoint = "/" + entityName + DOC + documentId;
                openSearchClient.sendRequest(endpoint, "DELETE", null);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }else{
                return userValidResponse;
            }
        }catch (IOException | InterruptedException e){
            logger.error(ERROR, e.getMessage());
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    public ResponseEntity<Object> getSingleData(String username, String password, String entityName,String documentId) {
        try {
            ResponseEntity<Object> userValidResponse = userService.isValidUser(username, password,entityName,"GET");
            if (userValidResponse.getStatusCode() == HttpStatus.OK) {
                String endpoint = "/" + entityName + DOC + documentId;
                response = openSearchClient.sendRequest(endpoint, "GET", null);
                JsonNode responseJson = objectMapper.readTree(response.body());

                if (responseJson.has(SOURCE)) {
                    ObjectNode sourceObject = (ObjectNode) responseJson.get(SOURCE); // Convert to ObjectNode
                    sourceObject.put("id", documentId);
                    return ResponseEntity.ok(sourceObject);
                } else {
                    return null;
                }
            }else{
                return userValidResponse;
            }

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public ResponseEntity<Object> getAllData(String username, String password, String entityName) throws IOException,InterruptedException{
        ResponseEntity<Object> userValidResponse = userService.isValidUser(username, password, entityName, "GET");

        if (userValidResponse.getStatusCode() == HttpStatus.OK) {
            Object responseBody = userValidResponse.getBody();
            Map<String, Object> responseData = objectMapper.convertValue(responseBody, new TypeReference<HashMap<String, Object>>() {});
            String tenant = (String) responseData.get(TENENT);
            String endpoint = "/" + entityName + "/_search?q=tenant:"+tenant;
            response = openSearchClient.sendRequest(endpoint, "GET", null);
            JsonNode responseJson = objectMapper.readTree(response.body());

            ArrayNode hitsArray = (ArrayNode) responseJson.at("/hits/hits");

            List<JsonNode> data = new ArrayList<>();
            for (JsonNode hit : hitsArray) {
                ObjectNode sourceObject = (ObjectNode) hit.get(SOURCE);
                String id = hit.get("_id").asText();
                sourceObject.put("id",id);
                data.add(sourceObject);
            }
            return ResponseEntity.ok(data);
        }else{
            return userValidResponse;
        }
    }

    public ResponseEntity<Object> updateData(String username, String password, String entityName,String id, Map<String, Object> updateData) throws IOException,InterruptedException {
        ResponseEntity<Object> userValidResponse = userService.isValidUser(username, password, entityName, "PUT");

        if (userValidResponse.getStatusCode() == HttpStatus.OK) {
            String jsonData = objectMapper.writeValueAsString(Map.of("doc", updateData));
            String endpoint = "/" + entityName + "/_update/" + id;
            HttpResponse<String> httpResponse =openSearchClient.sendRequest(endpoint, "POST", jsonData);
            String responseBody = httpResponse.body();
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(objectMapper.readValue(responseBody, Map.class));
        }else{
            return userValidResponse;
        }
    }

}
