package com.mcode.llp.codegen.initializer;

import com.mcode.llp.codegen.databases.OpenSearchClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.http.HttpResponse;

@Component
public class Initializer {

    HttpResponse<String> response;
    private final OpenSearchClient openSearchClient;
    private static final String ERROR = "An error occurred: {}";
    private static final Logger logger = LoggerFactory.getLogger(Initializer.class);
    private static final String SCHEMA_UPDATE_ENDPOINT="/schemas/_doc/";
    private static final String USER_INDEX = "users";
    private static final String PERMISSION_INDEX = "permission";

    public Initializer(OpenSearchClient openSearchClient) {
        this.openSearchClient = openSearchClient;
    }

    private void superUserSchemaInitialize() throws IOException,InterruptedException{
        // Check if the schema exists
        response = openSearchClient.sendRequest(SCHEMA_UPDATE_ENDPOINT+USER_INDEX,"GET", null);
        if(response.statusCode() == 404){
            String requestData = "{\"title\":\"users\",\"properties\":{\"username\":{\"type\":\"string\"},\"password\":{\"type\":\"string\"},\"role\":{\"type\":\"string\"},\"tenant\":{\"type\":\"string\"}},\"required\":[\"username\",\"password\",\"role\",\"tenant\"]}";
            response=openSearchClient.sendRequest(SCHEMA_UPDATE_ENDPOINT+USER_INDEX, "POST", requestData);
            if (response.statusCode() == 201) {
                logger.info("✅ User Schema created successfully.");
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error(ERROR, response.body());
                }
            }
        } else {
            logger.info("✅ User Schema already exists. Skipping initialization.");
        }
    }

    private void superUserIndexInitialize() throws IOException,InterruptedException{
        // Check if the index exists
        response = openSearchClient.sendRequest("/" + USER_INDEX, "GET", null);
        if (response.statusCode() == 404) {
            String requestData= "{\"username\":\"superadmin\",\"password\":\"SuperSecurePassword\",\"role\":\"superuser\",\"tenant\":\"global\"}";
            String endpoint = "/" + USER_INDEX + "/_doc/" + 1;
            response=openSearchClient.sendRequest(endpoint, "POST", requestData);
            if (response.statusCode() == 201) {
                logger.info("✅ superUser index created successfully.");
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error(ERROR, response.body());
                }
            }
        } else {
            logger.info("✅ User index already exists. Skipping initialization.");
        }
    }

    private void permissionSchemaInitialize() throws IOException,InterruptedException{
        // Check if the permission schema exists
        response = openSearchClient.sendRequest(SCHEMA_UPDATE_ENDPOINT+PERMISSION_INDEX,"GET", null);
        if(response.statusCode() == 404){
            String requestData = "{\"title\":\"permission\",\"properties\":{\"entity\":{\"type\":\"string\"},\"roles\":{\"type\":\"array\"},\"operation\":{\"type\":\"array\"}},\"required\":[\"entity\",\"roles\",\"operation\"]}";
            response=openSearchClient.sendRequest(SCHEMA_UPDATE_ENDPOINT+PERMISSION_INDEX, "POST", requestData);
            if (response.statusCode() == 201) {
                logger.info("✅ Permission Schema created successfully.");
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error(ERROR, response.body());
                }
            }
        } else {
            logger.info("✅ Permission Schema already exists. Skipping initialization.");
        }
    }

    private void permissionIndexInitialize() throws IOException,InterruptedException{
        // Check if the permission index exists
        response = openSearchClient.sendRequest("/" + PERMISSION_INDEX, "GET", null);
        if (response.statusCode() == 404) {
            String requestData = "{\"entity\":\"users\",\"roles\":[\"superuser\"],\"operation\":[\"POST\",\"PUT\",\"DELETE\",\"GET\"]}";
            String endpoint = "/" + PERMISSION_INDEX + "/_doc/" + 1;
            response=openSearchClient.sendRequest(endpoint, "POST", requestData);
            if (response.statusCode() == 201) {
                logger.info("✅ permission index created successfully.");
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error(ERROR, response.body());
                }
            }
        } else {
            logger.info("✅ permission index already exists. Skipping initialization.");
        }
    }

    public void superUserInitialize() {
        try {
            superUserSchemaInitialize();
            superUserIndexInitialize();
        } catch (IOException | InterruptedException e) {
            logger.error(ERROR, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    public void permissionInitialize(){
        try{
            permissionSchemaInitialize();
            permissionIndexInitialize();
        }catch (IOException | InterruptedException e){
            logger.error(ERROR, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
