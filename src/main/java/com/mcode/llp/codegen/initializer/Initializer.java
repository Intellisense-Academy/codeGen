package com.mcode.llp.codegen.initializer;

import com.mcode.llp.codegen.databases.OpenSearchClient;
import com.mcode.llp.codegen.services.SchemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.http.HttpResponse;

@Component
public class Initializer {

    HttpResponse<String> response;
    private final OpenSearchClient openSearchClient;
    private final SchemaService service;
    private static final String ERROR = "An error occurred: {}";
    private static final Logger logger = LoggerFactory.getLogger(Initializer.class);
    private static final String SCHEMA_UPDATE_ENDPOINT="/schemas/_doc/";
    private static final String USER_INDEX = "users";
    private static final String SETTINGS_INDEX = "settings";
    private static final String NOTIFICATION_INDEX = "notification";

    public Initializer(OpenSearchClient openSearchClient, SchemaService service) {
        this.openSearchClient = openSearchClient;
        this.service = service;
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
            String requestData= "{\"username\":\"superadmin\",\"password\":\"SuperSecurePassword\",\"role\":\"superuser\",\"tenant\":\"global\",\"id\":\"1\"}";
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
        response = openSearchClient.sendRequest(SCHEMA_UPDATE_ENDPOINT+ SETTINGS_INDEX,"GET", null);
        if(response.statusCode() == 404){
            String requestData = "{\"title\":\"settings\",\"type\":\"object\",\"properties\":{\"entity\":{\"type\":\"string\"},\"roles\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"allowedRoles\":{\"type\":\"array\",\"items\":{\"type\":\"string\"}},\"operations\":{\"type\":\"array\",\"items\":{\"type\":\"string\"}}},\"required\":[\"allowedRoles\",\"operations\"]}},\"notifications\":{\"type\":\"object\",\"properties\":{\"enabled\":{\"type\":\"boolean\"},\"content\":{\"type\":\"string\"},\"operations\":{\"type\":\"array\",\"items\":{\"type\":\"string\"}},\"to\":{\"type\":\"string\"}},\"required\":[\"enabled\",\"content\",\"operations\",\"to\"]}},\"required\":[\"entity\",\"roles\",\"notifications\"]}";
            response=openSearchClient.sendRequest(SCHEMA_UPDATE_ENDPOINT+ SETTINGS_INDEX, "POST", requestData);
            if (response.statusCode() == 201) {
                logger.info("✅ settings Schema created successfully.");
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error(ERROR, response.body());
                }
            }
        } else {
            logger.info("✅ settings Schema already exists. Skipping initialization.");
        }
    }

    private void permissionIndexInitialize() throws IOException,InterruptedException{
        // Check if the setting index exists
        response = openSearchClient.sendRequest("/" + SETTINGS_INDEX, "GET", null);
        if (response.statusCode() == 404) {
            String requestData = "{\"entity\":\"users\",\"roles\":[{\"allowedRoles\":[\"superuser\"],\"operations\":[\"POST\",\"PUT\",\"DELETE\",\"GET\"]}],\"notifications\":{\"enabled\":false,\"content\":\"Hi superuser\",\"operations\":[\"POST\",\"PUT\",\"DELETE\",\"GET\"],\"to\":\"+1234567890\"}}";
            String endpoint = "/" + SETTINGS_INDEX + "/_doc/" + 1;
            response=openSearchClient.sendRequest(endpoint, "POST", requestData);
            if (response.statusCode() == 201) {
                logger.info("✅ settings index created successfully.");
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error(ERROR, response.body());
                }
            }
        } else {
            logger.info("✅ settings index already exists. Skipping initialization.");
        }
    }

    private void notificationSchemaInitialize() throws IOException,InterruptedException{
        // Check if the schema exists
        response = openSearchClient.sendRequest(SCHEMA_UPDATE_ENDPOINT+NOTIFICATION_INDEX,"GET", null);
        if(response.statusCode() == 404){
            String requestData = "{\"title\":\"notification\",\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"},\"content\":{\"type\":\"string\"},\"receiver\":{\"type\":\"string\"}},\"required\":[\"name\",\"content\",\"receiver\"]}";
            response=openSearchClient.sendRequest(SCHEMA_UPDATE_ENDPOINT+NOTIFICATION_INDEX, "POST", requestData);
            if (response.statusCode() == 201) {
                logger.info("✅ Notification Schema created successfully.");
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error(ERROR, response.body());
                }
            }
        } else {
            logger.info("✅ Notification Schema already exists. Skipping initialization.");
        }
    }

    private void notificationPermissionInitialize() throws IOException,InterruptedException{
        String endpoint = "/settings/_doc/notification";
        response = openSearchClient.sendRequest(endpoint,"GET", null);
        if(response.statusCode() == 404){
            response = service.createDefaultSettingForSchema(NOTIFICATION_INDEX);
            if (response.statusCode() == 201) {
                logger.info("✅ notification settings index created successfully.");
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error(ERROR, response.body());
                }
            }
        }else{
            logger.info("✅ Notification settings already exists. Skipping initialization.");
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

    public void notificationInitialize(){
        try{
            notificationSchemaInitialize();
            notificationPermissionInitialize();
        }catch (IOException | InterruptedException e){
            logger.error(ERROR, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
