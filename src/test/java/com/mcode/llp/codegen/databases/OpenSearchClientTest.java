package com.mcode.llp.codegen.databases;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OpenSearchClientTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    @InjectMocks
    private OpenSearchClient openSearchClient;

    @Test
    void testSendRequest() throws IOException, InterruptedException {
        ReflectionTestUtils.setField(openSearchClient, "openSearchUrl", "http://dummy:9200");
        String mockResponseBody = "test";

        when(httpResponse.body()).thenReturn(mockResponseBody);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        HttpResponse<String> response = openSearchClient.sendRequest("/schemas", "GET", null);

        assertEquals("test", response.body());
    }
    @Test
    void postSendRequest() throws IOException, InterruptedException {
        ReflectionTestUtils.setField(openSearchClient, "openSearchUrl", "http://dummy:9200");
        String mockResponseBody = "post";

        when(httpResponse.body()).thenReturn(mockResponseBody);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        String requestBody = "{\"title\":\"testing\",\"properties\":{\"testerName\":{\"type\":\"string\"},\"password\":{\"type\":\"string\"}},\"required\":[\"testerName\",\"password\"]}";

        HttpResponse<String> response = openSearchClient.sendRequest("/schemas", "POST", requestBody);

        assertEquals("post", response.body());
    }
    @Test
    void deleteSendRequest() throws IOException, InterruptedException {
        ReflectionTestUtils.setField(openSearchClient, "openSearchUrl", "http://dummy:9200");
        String mockResponseBody = "delete";

        when(httpResponse.body()).thenReturn(mockResponseBody);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        String id = "1";

        HttpResponse<String> response = openSearchClient.sendRequest("/schemas/_doc/"+id, "DELETE", null);

        assertEquals("delete", response.body());
    }
    @Test
    void putSendRequest() throws IOException, InterruptedException {
        ReflectionTestUtils.setField(openSearchClient, "openSearchUrl", "http://dummy:9200");
        String mockResponseBody = "put";

        when(httpResponse.body()).thenReturn(mockResponseBody);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        String requestBody = "{\"title\":\"update-test\",\"properties\":{\"field\":{\"type\":\"text\"}}}";

        HttpResponse<String> response = openSearchClient.sendRequest("/schemas", "PUT", requestBody);

        assertEquals("put", response.body());
    }
    @Test
    void headSendRequest() throws IOException, InterruptedException {
        ReflectionTestUtils.setField(openSearchClient, "openSearchUrl", "http://dummy:9200");
        String mockResponseBody = "";

        when(httpResponse.body()).thenReturn(mockResponseBody);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        HttpResponse<String> response = openSearchClient.sendRequest("/schemas", "HEAD", null);

        assertEquals("", response.body());
    }
    
}

