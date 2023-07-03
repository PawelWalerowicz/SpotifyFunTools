package walerowicz.pawel.SpotifyFun.playlist.concurrent.search;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import walerowicz.pawel.SpotifyFun.authorization.AuthorizationException;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConcurrentSearchTest {

    private ConcurrentSearch concurrentSearch;
    private String mockURL;
    private final String token = "test-token";
    private final String regexCleanup = "[! .,-]";
    private final String trackResponse = getResource();

    private static MockWebServer mockWebServer;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @BeforeEach
    void reset() {
        mockURL = String.format("http://localhost:%s", mockWebServer.getPort());
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @NotNull
    private String getResource() {
        try (final var resourceAsStream = getClass()
                .getClassLoader()
                .getResourceAsStream("track_response.json")
        ) {
            final var bytes = Objects.requireNonNull(resourceAsStream).readAllBytes();
            return new String(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            return "Exception while getting test resource";
        }
    }

    @Test
    void shouldInitializeObjectInNotRunningState() {
        concurrentSearch = new ConcurrentSearch("query1", mockURL, "", token, new HashSet<>(), 1, 0);
        assertFalse(concurrentSearch.isRunning());
    }

    @Test
    void shouldChangeStateToIsRunningWhenRunWasCalled() {
        final var query = "query1";
        final var attemptLimit = 17;
        concurrentSearch = new ConcurrentSearch(query, mockURL, regexCleanup, token, new HashSet<>(), attemptLimit, 0);
        MockResponse mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(trackResponse)
                .setResponseCode(200);
        final var responses = Collections.nCopies(50, mockResponse);
        responses.forEach(response -> mockWebServer.enqueue(response));
        concurrentSearch.run();
        assertTrue(concurrentSearch.isRunning());
    }

    @Test
    void shouldChangeStateToNotRunningWhenShutDownWasCalled() {
        final var query = "query1";
        final var attemptLimit = 17;
        concurrentSearch = new ConcurrentSearch(query, mockURL, regexCleanup, token, new HashSet<>(), attemptLimit, 0);
        MockResponse mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(trackResponse)
                .setResponseCode(200);
        final var responses = Collections.nCopies(50, mockResponse);
        responses.forEach(response -> mockWebServer.enqueue(response));
        concurrentSearch.run();
        concurrentSearch.shutDown();
        assertFalse(concurrentSearch.isRunning());
    }

    @Test
    void shouldNotSendMoreRequestsThanGivenAttemptLimitEvenIfNoMatchIsFound() {
        final var query = "query1";
        final var attemptLimit = 17;
        concurrentSearch = new ConcurrentSearch(query, mockURL, regexCleanup, token, new HashSet<>(), attemptLimit, 0);
        MockResponse mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(trackResponse)
                .setResponseCode(200);
        final var responses = Collections.nCopies(50, mockResponse);
        responses.forEach(response -> mockWebServer.enqueue(response));
        concurrentSearch.run();
        assertEquals(attemptLimit, mockWebServer.getRequestCount());
    }

    @Test
    void shouldNotSendMoreRequestsWhenExactMatchIsFound() {
        final var query = "Title_2";
        final var attemptLimit = 50;
        concurrentSearch = new ConcurrentSearch(query, mockURL, regexCleanup, token, new HashSet<>(), attemptLimit, 0);
        MockResponse mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(trackResponse)
                .setResponseCode(200);
        final var responses = Collections.nCopies(50, mockResponse);
        responses.forEach(response -> mockWebServer.enqueue(response));
        concurrentSearch.run();
        assertEquals(1, mockWebServer.getRequestCount());
    }

    @Test
    void shouldWaitGivenPeriodAndRetryWhenResponseStatusIs429() {
        final var query = "Title_2";
        final var attemptLimit = 20;
        concurrentSearch = new ConcurrentSearch(query, mockURL, regexCleanup, token, new HashSet<>(), attemptLimit, 0);
        final var mockResponse = new MockResponse()
                .setResponseCode(429);
        final var responses = Collections.nCopies(50, mockResponse);
        responses.forEach(response -> mockWebServer.enqueue(response));
        concurrentSearch.run();
    }

    @Test
    void shouldThrowAuthorizationExceptionIfResponseStatusIs401() {
        final var query = "Title_2";
        final var attemptLimit = 50;
        concurrentSearch = new ConcurrentSearch(query, mockURL, regexCleanup, token, new HashSet<>(), attemptLimit, 0);
        final var mockResponse = new MockResponse()
                .setResponseCode(401);
        mockWebServer.enqueue(mockResponse);

        assertThrows(AuthorizationException.class, () -> concurrentSearch.run());
    }
}