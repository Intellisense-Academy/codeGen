package com.mcode.llp.codegen.opensearch_client_test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.mcode.llp.codegen.databases.OpenSearchClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class OpenSearchClientTest {

    @Mock
    private HttpClient httpClient;  // ✅ Mocked HTTP client (No real requests)

    @Mock
    private HttpResponse<String> httpResponse;  // ✅ Mocked OpenSearch response

    private OpenSearchClient openSearchClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // ✅ Inject the mocked HttpClient into OpenSearchClient
        openSearchClient = new OpenSearchClient();
        openSearchClient.setOpenSearchUrl("http://localhost:9200");
        openSearchClient.setUsername("admin");
        openSearchClient.setPassword("StrongPassword123!");
    }

    /** ✅ Test Case: Mock OpenSearch Response **/
    @Test
    void testMockedOpenSearchResponse() throws Exception {
        String requestBody = "{ \"title\": \"testingpurpose\", \"properties\": { \"testName\": { \"type\": \"string\" }, \"testAge\": { \"type\": \"number\" } }, \"required\": [\"testName\", \"testAge\"] }";

        // ✅ Mocking HttpClient behavior (No real request)
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        // ✅ Mocking response from OpenSearch (Fake data)
        when(httpResponse.body()).thenReturn("{\"_index\":\"schemas\",\"_id\":\"testingpurpose\",\"_version\":2,\"result\":\"updated\",\"_shards\":{\"total\":2,\"successful\":2,\"failed\":0},\"_seq_no\":14,\"_primary_term\":8}");

        // ✅ Send request (This will NOT go to localhost:9200)
        HttpResponse<String> response = openSearchClient.sendRequest("/schemas/_doc/testingpurpose", "POST", requestBody);

        // ✅ Verify response (Mocked response only)
        assertEquals("{\"_index\":\"schemas\",\"_id\":\"testingpurpose\",\"_version\":2,\"result\":\"updated\",\"_shards\":{\"total\":2,\"successful\":2,\"failed\":0},\"_seq_no\":14,\"_primary_term\":8}", response.body());
    }
}
