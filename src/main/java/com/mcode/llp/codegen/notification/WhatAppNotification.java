package com.mcode.llp.codegen.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Component
public class WhatAppNotification {

    @Value("${greenapi.url}")
    private String greenApiUrl;

    @Value("${greenapi.instance}")
    private String idInstance;

    @Value("${greenapi.token}")
    private String apiToken;

    private static final Logger logger = LoggerFactory.getLogger(WhatAppNotification.class);

    public boolean sendWhatsAppMessage(String phoneNumber, String message) {
        try {
            String chatId = phoneNumber + "@c.us";

            URL url = new URL(greenApiUrl + "/waInstance" + idInstance + "/sendMessage/" + apiToken);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonMessage = message
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n");

            String payload = String.format(
                    "{\"chatId\":\"%s\",\"message\":\"%s\"}",
                    chatId, jsonMessage
            );

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                return true;
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    errorResponse.append(line);
                }
                logger.error("Message failed: {}",errorResponse);
                return false;
            }

        } catch (Exception e) {
            logger.error("WhatAppMessage sending failed: " + e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        return false;
    }
}
