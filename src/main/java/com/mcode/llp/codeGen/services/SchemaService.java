package com.mcode.llp.codeGen.services;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcode.llp.codeGen.models.Schema;
import com.mcode.llp.codeGen.databases.OpenSearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.*;

@Service
public class SchemaService {

    @Autowired
    private OpenSearchClient openSearchClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String insertSchema(String index, String id, Schema schemaData) throws Exception {
        String jsonData = objectMapper.writeValueAsString(schemaData);
        String endpoint = "/" + index + "/_doc/" + id;
        return openSearchClient.sendRequest(endpoint, "POST", jsonData);
    }

    public String updateSchema(String id, Schema schemaData) throws Exception {
        String jsonData = objectMapper.writeValueAsString(Map.of("doc", schemaData));
        String endpoint = "/schemas/_update/" + id;
        return openSearchClient.sendRequest(endpoint, "POST", jsonData);
    }


    public JsonNode getAllSchema() throws Exception {
        String endpoint = "/schemas/_search?filter_path=hits.hits._source";

        String response = openSearchClient.sendRequest(endpoint, "GET", null);
        JsonNode responseJson = objectMapper.readTree(response);

        JsonNode hitsArray = responseJson.at("/hits/hits");

        List<JsonNode> schemas = new ArrayList<>();
        for (JsonNode hit : hitsArray) {
            JsonNode sourceObject = hit.get("_source");
            schemas.add(sourceObject);
        }

        return objectMapper.valueToTree(schemas);
    }

    public JsonNode getSchema(String entityName) {
        String endpoint = "/schemas/_doc/" + entityName;
        try {
            String response = openSearchClient.sendRequest(endpoint, "GET", null);

            JsonNode responseJson = objectMapper.readTree(response);

            if (responseJson.has("_source")) {
                return responseJson.get("_source");
            } else {
                throw new Exception("Document not found or missing _source");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String deleteSchema(String id) throws Exception {
        String endpoint = "/schemas/_doc/"+id;
        return openSearchClient.sendRequest(endpoint, "DELETE", null);
    }
}
