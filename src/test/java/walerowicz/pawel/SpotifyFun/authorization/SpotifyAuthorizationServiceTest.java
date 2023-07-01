package walerowicz.pawel.SpotifyFun.authorization;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import walerowicz.pawel.SpotifyFun.configuration.ClientSecretLoader;
import walerowicz.pawel.SpotifyFun.playlist.concurrent.search.TooManyRequestsException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpotifyAuthorizationServiceTest {
    private SpotifyAuthorizationService service;
    private static MockWebServer mockWebServer;

    @Mock
    private ClientSecretLoader secretLoader;

    @BeforeAll
    static void beforeAll() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @BeforeEach
    void setUp() {
        final var webClient = WebClient.builder()
                .baseUrl(String.format("http://localhost:%s/token", mockWebServer.getPort()))
                .defaultHeaders(header -> header.setContentType(MediaType.APPLICATION_FORM_URLENCODED))
                .build();
        when(secretLoader.loadSpotifyClientSecret()).thenReturn("test-secret");
        service = new SpotifyAuthorizationService(secretLoader,
                "clientId",
                "redirectURI",
                webClient
        );
        service.setEncodedAuthorizationCredentials();
    }

    @AfterAll
    static void afterAll() throws IOException {
        mockWebServer.close();
    }

    @Test
    void authorizationUrlShouldContainAllNecessaryParams() {
        service.setAuthorizationCodeURL();
        final var authorizationCodeURL = service.getAuthorizationCodeURL();

        assertTrue(authorizationCodeURL.contains("client_id"));
        assertTrue(authorizationCodeURL.contains("response_type"));
        assertTrue(authorizationCodeURL.contains("redirect_uri"));
        assertTrue(authorizationCodeURL.contains("scope"));
    }

    @Test
    void setAuthorizationCodeUrlShouldThrowAuthorizationExceptionWhenUnsupportedEncodingExceptionsOccurredDuringParamBuilding() {
        try (MockedStatic<URLEncoder> urlEncoderMockedStatic = mockStatic(URLEncoder.class)) {
            urlEncoderMockedStatic.when(() -> URLEncoder.encode(any(String.class), any(String.class)))
                    .thenThrow(new UnsupportedEncodingException("test exception"));

            assertThrows(AuthorizationException.class, () -> service.setAuthorizationCodeURL());
        }
    }

    @Test
    void fetchAccessTokenShouldSendPostRequest() throws InterruptedException {
        final var expectedTokenJSON =
                "{" +
                        "\"access_token\": \"TEST_TOKEN\"," +
                        "\"token_type\": \"Bearer\"," +
                        "\"expires_in\": 3600" +
                        "}";
        final var mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(expectedTokenJSON)
                .setResponseCode(200);
        mockWebServer.enqueue(mockResponse);

        service.fetchAccessToken("test-auth-code");

        final var recordedRequest = mockWebServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
    }

    @Test
    void fetchAccessTokenShouldSendRequestWithEncodedCredentialsInHeader() throws InterruptedException {
        final var expectedTokenJSON =
                "{" +
                        "\"access_token\": \"TEST_TOKEN\"," +
                        "\"token_type\": \"Bearer\"," +
                        "\"expires_in\": 3600" +
                        "}";
        final var encodedCredentials = Base64.getEncoder().encodeToString("clientId:test-secret".getBytes());
        final var mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(expectedTokenJSON)
                .setResponseCode(200);
        mockWebServer.enqueue(mockResponse);
        service.fetchAccessToken("test-auth-code");

        final var recordedRequest = mockWebServer.takeRequest();
        assertEquals("Basic " + encodedCredentials, recordedRequest.getHeaders().get("Authorization"));
    }

    @Test
    void fetchAccessTokenShouldThrowTooManyRequestsExceptionWhenResponseStatusCodeIs429() {
        final var mockResponse = new MockResponse().setResponseCode(429);
        mockWebServer.enqueue(mockResponse);

        assertThrows(TooManyRequestsException.class, () -> service.fetchAccessToken("test-auth-code"));
    }

    @Test
    void fetchAccessTokenShouldThrowAuthorizationExceptionWhenResponseStatusCodeIs401() {
        final var mockResponse = new MockResponse().setResponseCode(401);
        mockWebServer.enqueue(mockResponse);

        assertThrows(AuthorizationException.class, () -> service.fetchAccessToken("test-auth-code"));
    }

}