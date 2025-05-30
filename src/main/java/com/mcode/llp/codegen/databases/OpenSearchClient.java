package com.mcode.llp.codegen.databases;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class OpenSearchClient {

    @Value("${opensearch.url}")
    private String openSearchUrl;

    @Value("${opensearch.username}")
    private String username;

    @Value("${opensearch.password}")
    private String password;

    private final HttpClient httpClient;

    public OpenSearchClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Autowired
    public OpenSearchClient() {
        this.httpClient = HttpClient.newHttpClient();
    }

    private String getAuthHeader() {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    public HttpResponse<String> sendRequest(String endpoint, String method, String body) throws IOException,InterruptedException{
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(openSearchUrl + endpoint))
                .header("Authorization", getAuthHeader())
                .header("Content-Type", "application/json");

        switch (method.toUpperCase()) {
            case "POST":
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body));
                break;
            case "PUT":
                requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(body));
                break;
            case "GET":
                if (body != null && !body.isEmpty()) {
                    throw new IllegalArgumentException("GET request should not have a body!");
                }
                requestBuilder.GET();
                break;
            case "DELETE":
                requestBuilder.DELETE();
                break;
            case "HEAD":
                requestBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
                break;
            default:
                throw new IllegalArgumentException("Invalid HTTP method: " + method);
        }


        HttpRequest request = requestBuilder.build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
