package com.mcode.llp.codegen.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcode.llp.codegen.models.TenantRequest;
import com.mcode.llp.codegen.databases.OpenSearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class TenantService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private OpenSearchClient openSearchClient;

    public void createTenant(TenantRequest request) throws InterruptedException, IOException {
        // 1. Generate credentials
        String email = request.getEmail();
        String tenant = request.getTenantName();
        String password = generateRandomPassword();
        String documentId = UUID.randomUUID().toString();

        // 2. Save to 'tenant' index
        Map<String, Object> tenantData = new HashMap<>();
        tenantData.put("email", email);
        tenantData.put("password", password);
        tenantData.put("role","admin");
        tenantData.put("tenant",tenant);
        tenantData.put("id",documentId);

        String userJson = objectMapper.writeValueAsString(tenantData);

        HttpResponse<String> r1 = openSearchClient.sendRequest("/users/_doc/"+documentId, "POST", userJson);

        // 3. Add notification
        String documentId2 = UUID.randomUUID().toString();
        Map<String, Object> notification = new HashMap<>();
        notification.put("name", "unpaid");
        notification.put("content", "Hi ${name},This is a reminder that your monthly contribution is still unpaid. Kindly complete the payment at your earliest convenience.If you’ve already paid, please ignore this message.Thank you,${tenant} Team");
        notification.put("receiver","${phNo}");
        notification.put("sendDays",Arrays.asList(1, 2, 5, 7, 10, 15, 20, 30));
        notification.put("tenant",tenant);
        notification.put("id",documentId2);

        String notificationJson = objectMapper.writeValueAsString(notification);

        HttpResponse<String> r2 = openSearchClient.sendRequest("/notification/_doc/"+documentId2, "POST", notificationJson);
    }

    private String generateRandomPassword() {
        return UUID.randomUUID().toString().substring(0, 8); // Example password
    }
}

