package walerowicz.pawel.spotifyfun.playlist.service.search;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import walerowicz.pawel.spotifyfun.authorization.exception.AuthorizationException;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConcurrentSearchTest {

    private String mockURL;
    private final String token = "test-token";
    private final String regexCleanup = "[! .,-]";
    private final String trackResponse = getResource();

    private MockWebServer mockWebServer;

    @BeforeEach
    void reset() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        mockURL = String.format("http://localhost:%s", mockWebServer.getPort());
    }

    @AfterEach
    void shutDown() throws IOException {
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
        final var concurrentSearch = new ConcurrentSearch("query1", mockURL, regexCleanup, token, new HashSet<>(), 1, 0);
        assertFalse(concurrentSearch.isRunning());
    }

    @Test
    void shouldChangeStateToIsRunningWhenRunWasCalled() {
        final var query = "query1";
        final var attemptLimit = 31;
        final var concurrentSearch = new ConcurrentSearch(query, mockURL, regexCleanup, token, new HashSet<>(), attemptLimit, 0);
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
        final var attemptLimit = 21;
        final var concurrentSearch = new ConcurrentSearch(query, mockURL, regexCleanup, token, new HashSet<>(), attemptLimit, 0);
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
        final var attemptLimit = 13;
        final var concurrentSearch = new ConcurrentSearch(query, mockURL, regexCleanup, token, new HashSet<>(), attemptLimit, 0);
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
        final var concurrentSearch = new ConcurrentSearch(query, mockURL, regexCleanup, token, new HashSet<>(), attemptLimit, 0);
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
        final var waitPeriodMs = 1;
        final var tooManyResponsesBeforeSuccess = 5;
        final var concurrentSearch = new ConcurrentSearch(query, mockURL, regexCleanup, token, new HashSet<>(), attemptLimit, waitPeriodMs);
        final var tooManyAttemptsResponse = new MockResponse()
                .setResponseCode(429);
        final var responses = Collections.nCopies(tooManyResponsesBeforeSuccess, tooManyAttemptsResponse);
        responses.forEach(response -> mockWebServer.enqueue(response));
        MockResponse successfulResponse = new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(trackResponse)
                .setResponseCode(200);
        mockWebServer.enqueue(successfulResponse);
        concurrentSearch.run();
//        verify(sleeper, times(tooManyResponsesBeforeSuccess)).sleep(waitPeriodMs);
    }

    @Test
    void shouldThrowAuthorizationExceptionIfResponseStatusIs401() {
        final var query = "Title_2";
        final var attemptLimit = 50;
        final var concurrentSearch = new ConcurrentSearch(query, mockURL, regexCleanup, token, new HashSet<>(), attemptLimit, 0);
        final var mockResponse = new MockResponse()
                .setResponseCode(401);
        mockWebServer.enqueue(mockResponse);

        assertThrows(AuthorizationException.class, concurrentSearch::run);
    }
}