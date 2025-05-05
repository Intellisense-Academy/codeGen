package com.mcode.llp.codegen.services;

import com.mcode.llp.codegen.databases.OpenSearchClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private OpenSearchClient openSearchClient;

    @Mock
    private HttpResponse<String> httpResponse;

    @Mock
    private HttpResponse<String> httpResponse1;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(openSearchClient);
    }

    @Test
    void testExtractCredentials_ValidHeader() {
        String authHeader = "Basic " + java.util.Base64.getEncoder().encodeToString("testUser:testPass".getBytes());

        String[] credentials = userService.extractCredentials(authHeader);

        assertEquals("testUser", credentials[0]);
        assertEquals("testPass", credentials[1]);
    }

    @Test
    void testExtractCredentials_InvalidHeader() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.extractCredentials("InvalidHeader");
        });

        assertEquals("Missing or invalid Authorization header", exception.getMessage());
    }

    @Test
    void testIsValidUser_Success() throws IOException, InterruptedException {
        String username = "testUser";
        String password = "testPass";
        String entityName = "users"; // Important: match the entity from your requestData
        String operation = "GET";    // Use operation that exists

        // Mock user search response
        String userSearchResponse = "{ \"hits\": { \"hits\": [ { \"_source\": { \"username\": \"testUser\", \"password\": \"testPass\", \"role\": \"superuser\", \"tenant\": \"global\" } } ] } }";
        when(openSearchClient.sendRequest(eq("/users/_search?q=username:" + username), eq("GET"), isNull()))
                .thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn(userSearchResponse);

        // Mock permission (settings) search response
        String permissionSearchResponse = "{ \"hits\": { \"hits\": [ { \"_source\": { \"entity\": \"users\", \"roles\": [ { \"allowedRoles\": [\"superuser\"], \"operations\": [\"POST\", \"PUT\", \"DELETE\", \"GET\"] } ], \"notifications\": { \"enabled\": false, \"content\": \"Hi superuser\", \"operations\": [\"POST\", \"PUT\", \"DELETE\", \"GET\"], \"to\": \"+1234567890\" } } } ] } }";
        when(openSearchClient.sendRequest(eq("/settings/_search?q=entity:" + entityName), eq("GET"), isNull()))
                .thenReturn(httpResponse1);
        when(httpResponse1.body()).thenReturn(permissionSearchResponse);

        // Call your service
        ResponseEntity<Object> response = userService.isValidUser(username, password, entityName, operation);

        // Validate
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        assertEquals("global", ((Map<String, Object>) response.getBody()).get("tenant"));
    }




    @Test
    void testIsValidUser_InvalidPassword() throws IOException, InterruptedException {
        String username = "testUser";
        String password = "wrongPass";
        String entityName = "testEntity";
        String operation = "read";

        String jsonResponse = "{ \"hits\": { \"hits\": [ { \"_source\": { \"username\": \"testUser\", \"password\": \"testPass\", \"role\": \"admin\", \"tenant\": \"global\" } } ] } }";

        when(openSearchClient.sendRequest(anyString(), eq("GET"), isNull())).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn(jsonResponse);

        ResponseEntity<Object> response = userService.isValidUser(username, password, entityName, operation);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid username or password", ((Map<String, String>) response.getBody()).get("message"));
    }

    @Test
    void testIsValidUser_NotFound() throws IOException, InterruptedException {
        when(openSearchClient.sendRequest(anyString(), eq("GET"), isNull())).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn("{ \"hits\": { \"hits\": [] } }");

        ResponseEntity<Object> response = userService.isValidUser("testUser", "testPass", "testEntity", "read");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No user found", ((Map<String, String>) response.getBody()).get("message"));
    }

    @Test
    void testIsAuthorizedUser_Authorized() throws IOException, InterruptedException {
        // Corrected JSON: roles is an array
        String jsonResponse = "{ \"hits\": { \"hits\": [ { \"_source\": { \"roles\": [ { \"allowedRoles\": [\"admin\"], \"operations\": [\"read\", \"write\"] } ] } } ] } }";

        when(openSearchClient.sendRequest(anyString(), eq("GET"), isNull())).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn(jsonResponse);

        boolean result = userService.isAuthorizedUser("testEntity", "admin", "read");

        assertTrue(result); // ✅ Now it will pass correctly
    }


    @Test
    void testIsAuthorizedUser_NotAuthorized() throws IOException, InterruptedException {
        String jsonResponse = "{ \"hits\": { \"hits\": [ { \"_source\": { \"roles\": [\"admin\"], \"operation\": [\"write\"] } } ] } }";

        when(openSearchClient.sendRequest(anyString(), eq("GET"), isNull())).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn(jsonResponse);

        boolean result = userService.isAuthorizedUser("testEntity", "admin", "read");

        assertFalse(result);
    }

    @Test
    void testIsAuthorizedUser_NoPermissions() throws IOException, InterruptedException {
        when(openSearchClient.sendRequest(anyString(), eq("GET"), isNull())).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn("{ \"hits\": { \"hits\": [] } }");

        boolean result = userService.isAuthorizedUser("testEntity", "admin", "read");

        assertFalse(result);
    }
}
