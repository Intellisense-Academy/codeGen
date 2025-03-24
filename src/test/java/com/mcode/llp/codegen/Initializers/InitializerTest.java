package com.mcode.llp.codegen.Initializers;

import com.mcode.llp.codegen.databases.OpenSearchClient;
import com.mcode.llp.codegen.initializer.Initializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.net.http.HttpResponse;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InitializerTest {

    @Mock
    private OpenSearchClient openSearchClient;

    @Mock
    private HttpResponse<String> mockResponse;

    @InjectMocks
    private Initializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new Initializer(openSearchClient);
    }

    @Test
    void testSuperUserInitialize_WhenSchemaAndIndexExist_ShouldSkipInitialization() throws IOException, InterruptedException {
        // Mock schema exists (returns 200)
        when(openSearchClient.sendRequest(contains("/schemas/_doc/users"), eq("GET"), isNull()))
                .thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);

        // Mock index exists (returns 200)
        when(openSearchClient.sendRequest(eq("/users"), eq("GET"), isNull()))
                .thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);

        initializer.superUserInitialize();

        // Verify schema check
        verify(openSearchClient).sendRequest(contains("/schemas/_doc/users"), eq("GET"), isNull());
        verify(openSearchClient, never()).sendRequest(contains("/schemas/_doc/users"), eq("POST"), anyString());

        // Verify index check
        verify(openSearchClient).sendRequest(eq("/users"), eq("GET"), isNull());
        verify(openSearchClient, never()).sendRequest(contains("/users/_doc/1"), eq("POST"), anyString());
    }

    @Test
    void testPermissionInitialize_WhenSchemaAndIndexExist_ShouldSkipInitialization() throws IOException, InterruptedException {
        // Mock schema exists (returns 200)
        when(openSearchClient.sendRequest(contains("/schemas/_doc/permission"), eq("GET"), isNull()))
                .thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);

        // Mock index exists (returns 200)
        when(openSearchClient.sendRequest(eq("/permission"), eq("GET"), isNull()))
                .thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);

        // Execute the initialization method
        initializer.permissionInitialize();

        // Verify schema check
        verify(openSearchClient).sendRequest(contains("/schemas/_doc/permission"), eq("GET"), isNull());
        verify(openSearchClient, never()).sendRequest(contains("/schemas/_doc/permission"), eq("POST"), anyString());

        // Verify index check
        verify(openSearchClient).sendRequest(eq("/permission"), eq("GET"), isNull());
        verify(openSearchClient, never()).sendRequest(contains("/permission/_doc/1"), eq("POST"), anyString());
    }

}
