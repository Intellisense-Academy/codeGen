package com.mcode.llp.codeGen.services;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcode.llp.codeGen.models.Schema;
import com.mcode.llp.codeGen.databases.OpenSearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
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

    private Schema convertJsonToSchema(JSONObject json) {
        Schema schema = new Schema();
        schema.setTitle(json.optString("title"));
        schema.setType(json.optString("type", "object")); // Default type to "object"

        // Convert properties
        if (json.has("properties")) {
            JSONObject propertiesJson = json.getJSONObject("properties");
            Map<String, Schema> properties = new HashMap<>();
            for (String key : propertiesJson.keySet()) {
                properties.put(key, convertJsonToSchema(propertiesJson.getJSONObject(key)));
            }
            schema.setProperties(properties);
        }

        // Convert required fields
        if (json.has("required")) {
            JSONArray requiredArray = json.getJSONArray("required");
            Set<String> requiredFields = new HashSet<>();
            for (int i = 0; i < requiredArray.length(); i++) {
                requiredFields.add(requiredArray.getString(i));
            }
            schema.setRequired(requiredFields);
        }

        return schema;
    }

    public List<Schema> getAllSchema() throws Exception {
        String endpoint = "/schemas/_search?filter_path=hits.hits._source";
        String requestBody = "{ \"query\": { \"match_all\": {} }, \"_source\": true }";

        String response = openSearchClient.sendRequest(endpoint, "POST", requestBody);

        JSONObject jsonObject = new JSONObject(response);
        JSONArray hitsArray = jsonObject.getJSONObject("hits").getJSONArray("hits");

        List<Schema> schemas = new ArrayList<>();
        for (int i = 0; i < hitsArray.length(); i++) {
            JSONObject sourceObject = hitsArray.getJSONObject(i).getJSONObject("_source");

            // Convert JSON to Schema object
            Schema schema = convertJsonToSchema(sourceObject);
            schemas.add(schema);
        }

        return schemas;

    }

    public Schema getSchema(String entityName) {
        String endpoint = "/schemas/_search";
        try {
            String requestBody = "{ \"query\": { \"term\": { \"_id\": \"" + entityName + "\" } }, \"_source\": true }";
            String response = openSearchClient.sendRequest(endpoint, "POST", requestBody);
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("hits") && jsonObject.getJSONObject("hits").has("hits")) {
                JSONObject sourceObject = jsonObject.getJSONObject("hits").getJSONArray("hits").getJSONObject(0).getJSONObject("_source");
                Schema schema = convertJsonToSchema(sourceObject);
                return schema;
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
