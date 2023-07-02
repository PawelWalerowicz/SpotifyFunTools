package walerowicz.pawel.SpotifyFun.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import walerowicz.pawel.SpotifyFun.authorization.AuthorizationException;
import walerowicz.pawel.SpotifyFun.authorization.entites.User;
import walerowicz.pawel.SpotifyFun.playlist.concurrent.search.TooManyRequestsException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {
    private UserService userService;

    private static MockWebServer mockWebServer;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @BeforeEach
    void reset() {
        userService = new UserService(WebClient.builder()
                .baseUrl(String.format("http://localhost:%s", mockWebServer.getPort()))
                .defaultHeaders(header -> header.setContentType(MediaType.APPLICATION_JSON))
                .build());
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldSendSpotifyApiRequestToMeEndpoint() throws InterruptedException {
        final var mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody("{\"id\":\"Test user id\"}")
                .setResponseCode(200);
        mockWebServer.enqueue(mockResponse);

        userService.importUser("test-token");
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/me", recordedRequest.getPath());
    }

    @Test
    void shouldSendSpotifyApiRequestWithTokenInAuthorizationHeader() throws InterruptedException {
        final var mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody("{\"id\":\"Test user id\"}")
                .setResponseCode(200);
        mockWebServer.enqueue(mockResponse);

        userService.importUser("test-token");
        final var recordedRequest = mockWebServer.takeRequest();
        assertEquals("Bearer test-token", recordedRequest.getHeaders().get("Authorization"));
    }

    @Test
    void shouldSendGetRequest() throws InterruptedException {
        final var mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody("{\"id\":\"Test user id\"}")
                .setResponseCode(200);
        mockWebServer.enqueue(mockResponse);

        userService.importUser("test-token");
        final var recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    void shouldThrowTooManyRequestsExceptionIfResponseStatusIs429() {
        final var mockResponse = new MockResponse()
                .setResponseCode(429);
        mockWebServer.enqueue(mockResponse);

        assertThrows(TooManyRequestsException.class, () -> userService.importUser("test-token"));
    }

    @Test
    void shouldThrowAuthorizationExceptionIfResponseStatusIs401() {
        final var mockResponse = new MockResponse()
                .setResponseCode(401);
        mockWebServer.enqueue(mockResponse);

        assertThrows(AuthorizationException.class, () -> userService.importUser("test-token"));
    }


}