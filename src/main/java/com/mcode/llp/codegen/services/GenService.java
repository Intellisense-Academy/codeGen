package com.mcode.llp.codegen.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcode.llp.codegen.databases.OpenSearchClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GenService {
    private final OpenSearchClient openSearchClient;
    private static final String SOURCE = "_source";
    private static final String DOC = "/_doc/";

    @Autowired
    public GenService(OpenSearchClient openSearchClient){
        this.openSearchClient=openSearchClient;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(GenService.class);

    public boolean indexExists(String entityName){
        try{
            HttpResponse<String> response = openSearchClient.sendRequest("/"+entityName ,"HEAD", null);
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            logger.error(e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
    }


    public HttpResponse<String> insertData(String schemaName, String documentId, JsonNode data) throws IOException,InterruptedException {
        String endpoint = "/" + schemaName + DOC + documentId;
        String requestBody = objectMapper.writeValueAsString(data);
        return openSearchClient.sendRequest(endpoint, "POST", requestBody);
    }

    public void deleteData(String entityName,String documentId) throws IOException,InterruptedException {
        String endpoint = "/" + entityName + DOC +documentId;
        openSearchClient.sendRequest(endpoint, "DELETE", null);
    }

    public JsonNode getSingleData(String entityName,String documentId) {
        String endpoint = "/" + entityName + DOC + documentId;
        try {
            HttpResponse<String> response = openSearchClient.sendRequest(endpoint, "GET", null);

            JsonNode responseJson = objectMapper.readTree(response.body());

            if (responseJson.has(SOURCE)) {
                ObjectNode sourceObject = (ObjectNode) responseJson.get(SOURCE); // Convert to ObjectNode
                sourceObject.put("id", documentId);
                return sourceObject;
            } else {
                return null;
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public List<JsonNode> getAllData(String entityName) throws IOException,InterruptedException{
        String endpoint = "/" + entityName + "/_search?filter_path=hits.hits";

        HttpResponse<String> response = openSearchClient.sendRequest(endpoint, "GET", null);
        JsonNode responseJson = objectMapper.readTree(response.body());

        ArrayNode hitsArray = (ArrayNode) responseJson.at("/hits/hits");

        List<JsonNode> data = new ArrayList<>();
        for (JsonNode hit : hitsArray) {
            ObjectNode sourceObject = (ObjectNode) hit.get(SOURCE);
            String id = hit.get("_id").asText();
            sourceObject.put("id",id);
            data.add(sourceObject);
        }
        return data;
    }

    public HttpResponse<String> updateData(String entityName,String id, Map<String, Object> updateData) throws IOException,InterruptedException {
        String jsonData = objectMapper.writeValueAsString(Map.of("doc", updateData));
        String endpoint = "/" + entityName + "/_update/" + id;
        return openSearchClient.sendRequest(endpoint, "POST", jsonData);
    }

}
