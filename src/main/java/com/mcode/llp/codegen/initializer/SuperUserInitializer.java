package com.mcode.llp.codegen.initializer;

import com.mcode.llp.codegen.databases.OpenSearchClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.http.HttpResponse;

@Component
public class SuperUserInitializer {

    HttpResponse<String> response;
    private final OpenSearchClient openSearchClient;
    private static final String ERROR = "An error occurred: {}";
    private static final Logger logger = LoggerFactory.getLogger(SuperUserInitializer.class);
    private static final String schemaEndPoint="/schemas/_doc/users";
    private static final String USER_INDEX = "users";

    public SuperUserInitializer(OpenSearchClient openSearchClient) {
        this.openSearchClient = openSearchClient;
    }

    public void initializeOpenSearch() {
        try {
            // Check if the schema exists
            response = openSearchClient.sendRequest(schemaEndPoint,"GET", null);
            if(response.statusCode() == 404){
                String requestData = "{\"title\":\"users\",\"properties\":{\"username\":{\"type\":\"string\"},\"password\":{\"type\":\"string\"},\"role\":{\"type\":\"string\"},\"tenant\":{\"type\":\"string\"}},\"required\":[\"username\",\"password\",\"role\",\"tenant\"]}";
                response=openSearchClient.sendRequest(schemaEndPoint, "POST", requestData);
                if (response.statusCode() == 201) {
                    logger.info("✅ User Schema created successfully.");
                } else {
                    logger.error(ERROR,response.body());
                }
            } else {
                logger.info("✅ User Schema already exists. Skipping initialization.");
            }
            // Check if the index exists
            response = openSearchClient.sendRequest("/" + USER_INDEX, "GET", null);
            if (response.statusCode() == 404) {
                String requestData= "{\"username\":\"superadmin\",\"password\":\"SuperSecurePassword\",\"role\":\"superuser\",\"tenant\":\"global\"}";
                String endpoint = "/" + USER_INDEX + "/_doc/" + 1;
               response=openSearchClient.sendRequest(endpoint, "POST", requestData);
                if (response.statusCode() == 201) {
                    logger.info("✅ superUser index created successfully.");
                } else {
                    logger.error(ERROR,response.body());
                }
            } else {
                logger.info("✅ User index already exists. Skipping initialization.");
            }

        } catch (IOException | InterruptedException e) {
            logger.error(ERROR, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
