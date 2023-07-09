package walerowicz.pawel.spotifyfun.playlist.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import walerowicz.pawel.spotifyfun.authorization.entity.User;
import walerowicz.pawel.spotifyfun.authorization.exception.AuthorizationException;
import walerowicz.pawel.spotifyfun.authorization.service.UserService;
import walerowicz.pawel.spotifyfun.playlist.entity.PlaylistRequest;
import walerowicz.pawel.spotifyfun.playlist.entity.Track;
import walerowicz.pawel.spotifyfun.playlist.entity.TracksWithPhrase;
import walerowicz.pawel.spotifyfun.playlist.exception.TooManyRequestsException;
import walerowicz.pawel.spotifyfun.playlist.exception.TracksNotFoundException;
import walerowicz.pawel.spotifyfun.playlist.service.combinations.CombinationMatcher;
import walerowicz.pawel.spotifyfun.test.utils.response.PlaylistResponseBuilder;
import walerowicz.pawel.spotifyfun.test.utils.response.TracksWithPhrasesTestBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest {
    private MockWebServer webServer;
    @Mock
    private UserService userService;
    @Mock
    private CombinationMatcher combinationMatcher;

    private PlaylistService service;
    private final User testUser = new User("userId");

    @BeforeEach
    void reset() throws IOException {
        webServer = new MockWebServer();
        webServer.start();

        final var webClient = WebClient.builder()
                .baseUrl(String.format("http://localhost:%s", webServer.getPort()))
                .defaultHeaders(header -> header.setContentType(MediaType.APPLICATION_JSON))
                .build();

        service = new PlaylistService(
                userService,
                combinationMatcher,
                webClient,
                0
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        webServer.close();
    }

    @Test
    void shouldCallCombinationMatcherOnceThenSendNewPlaylistRequestsThenSendAddTracksRequest() throws InterruptedException {
        final var testSentence = "Test Sentence";
        final var testToken = "test-token";
        when(userService.fetchUser(any(String.class))).thenReturn(testUser);
        final var tracksWithPhrases = List.of(
                new TracksWithPhrase("Test", List.of(new Track("id1", "href", "name1")))
        );
        when(combinationMatcher.findCombinationWithMatchingTracks(testSentence, testToken))
                .thenReturn(tracksWithPhrases);

        final var successfulNewPlaylistResponse = PlaylistResponseBuilder
                .newBuilder()
                .withProperPlaylist()
                .build();
        webServer.enqueue(successfulNewPlaylistResponse);

        final var successfulAddTracksResponse = new MockResponse().setResponseCode(201);
        webServer.enqueue(successfulAddTracksResponse);

        service.buildPlaylist(new PlaylistRequest("TestName", testSentence, testToken));

        verify(combinationMatcher, times(1)).findCombinationWithMatchingTracks(testSentence, testToken);
        assertEquals("/users/userId/playlists", webServer.takeRequest().getPath());
        assertEquals("/playlists/playlist-id/tracks", webServer.takeRequest().getPath());
        assertEquals(2, webServer.getRequestCount());
    }

    @Test
    void requestToCreatePlaylistShouldBePOSTRequestAndContainTokenInHeaderAndPlaylistNameInBody() throws InterruptedException {
        final var testSentence = "Test Sentence";
        final var testToken = "test-token";
        when(userService.fetchUser(any(String.class))).thenReturn(testUser);
        final var tracksWithPhrases = TracksWithPhrasesTestBuilder
                .newBuilder()
                .withPhraseWithMatchingTracks("Test", 1)
                .buildAsList();
        when(combinationMatcher.findCombinationWithMatchingTracks(testSentence, testToken)).thenReturn(tracksWithPhrases);
        final var successfulNewPlaylistResponse = PlaylistResponseBuilder
                .newBuilder()
                .withProperPlaylist()
                .build();
        webServer.enqueue(successfulNewPlaylistResponse);

        final var successfulAddTracksResponse = new MockResponse().setResponseCode(201);
        webServer.enqueue(successfulAddTracksResponse);

        service.buildPlaylist(new PlaylistRequest("TestName", testSentence, testToken));

        final RecordedRequest firstRequest = webServer.takeRequest();
        assertEquals("POST", firstRequest.getMethod());
        assertEquals("Bearer " + testToken, firstRequest.getHeaders().get("Authorization"));
        assertEquals("{\"name\":\"TestName\"}", firstRequest.getBody().readString(StandardCharsets.UTF_8));
    }

    @Test
    void requestToAddTracksShouldBePOSTRequestAndContainTokenInHeaderAndChosenTracksIdsInBody() throws InterruptedException {
        final var testSentence = "Test Sentence";
        final var testToken = "test-token";
        when(userService.fetchUser(any(String.class))).thenReturn(testUser);
        final var tracksWithPhrases = TracksWithPhrasesTestBuilder
                .newBuilder()
                .withPhraseWithMatchingTracks("Test", 1)
                .withPhraseWithMatchingTracks("Sentence", 1)
                .buildAsList();
        when(combinationMatcher.findCombinationWithMatchingTracks(testSentence, testToken)).thenReturn(tracksWithPhrases);
        final var successfulNewPlaylistResponse = PlaylistResponseBuilder
                .newBuilder()
                .withProperPlaylist()
                .build();
        webServer.enqueue(successfulNewPlaylistResponse);

        final var successfulAddTracksResponse = new MockResponse().setResponseCode(201);
        webServer.enqueue(successfulAddTracksResponse);

        service.buildPlaylist(new PlaylistRequest("TestName", testSentence, testToken));

        webServer.takeRequest();
        final var secondRequest = webServer.takeRequest();
        assertEquals("POST", secondRequest.getMethod());
        assertEquals("Bearer " + testToken, secondRequest.getHeaders().get("Authorization"));
        assertEquals(
                "[\"spotify:track:Test_id_1\",\"spotify:track:Sentence_id_2\"]",
                secondRequest.getBody().readString(StandardCharsets.UTF_8)
        );
    }

    @Test
    void shouldThrowTracksNotFoundExceptionIfCombinationMatcherReturnsEmptyList() {
        final var testSentence = "Test Sentence";
        final var testToken = "test-token";
        when(combinationMatcher.findCombinationWithMatchingTracks(testSentence, testToken))
                .thenReturn(Collections.emptyList());
        final var successfulNewPlaylistResponse = PlaylistResponseBuilder
                .newBuilder()
                .withProperPlaylist()
                .build();
        webServer.enqueue(successfulNewPlaylistResponse);

        final var successfulAddTracksResponse = new MockResponse().setResponseCode(201);
        webServer.enqueue(successfulAddTracksResponse);

        final var properPlaylistRequest = new PlaylistRequest("TestName", testSentence, testToken);

        assertThrows(TracksNotFoundException.class, () -> service.buildPlaylist(properPlaylistRequest));
    }

    @Test
    void shouldChoseSingleRandomTrackForEachPhraseFromReturnedList() throws InterruptedException {
        final var testSentence = "Test Sentence";
        final var testToken = "test-token";
        when(userService.fetchUser(any(String.class))).thenReturn(testUser);
        final var tracksWithPhrases = TracksWithPhrasesTestBuilder
                .newBuilder()
                .withPhraseWithMatchingTracks("Test", 10)
                .withPhraseWithMatchingTracks("Sentence", 10)
                .buildAsList();
        when(combinationMatcher.findCombinationWithMatchingTracks(testSentence, testToken)).thenReturn(tracksWithPhrases);
        final var successfulNewPlaylistResponse = PlaylistResponseBuilder
                .newBuilder()
                .withProperPlaylist()
                .build();
        webServer.enqueue(successfulNewPlaylistResponse);

        final var successfulAddTracksResponse = new MockResponse().setResponseCode(201);
        webServer.enqueue(successfulAddTracksResponse);

        service.buildPlaylist(new PlaylistRequest("TestName", testSentence, testToken));

        webServer.takeRequest();    //first request - creating playlist, not checked in this test
        final var secondRequestBody = webServer.takeRequest().getBody().readString(StandardCharsets.UTF_8);
        final List<String> singleTrackRequests = Arrays.stream(secondRequestBody
                .replaceAll("[\\[\\]\"]", "")
                .split(",")
        ).toList();

        assertTrue(singleTrackRequests.get(0).matches("^spotify:track:Test_id_[\\d]+$"));
        assertTrue(singleTrackRequests.get(1).matches("^spotify:track:Sentence_id_[\\d]+$"));
        assertEquals(2, singleTrackRequests.size());
    }


    @Test
    void shouldThrowTooManyRequestsExceptionWhenCreateNewPlaylistResponseStatusIs429ForMoreThan5Attempts() {
        final var testSentence = "Test Sentence";
        final var testToken = "test-token";
        when(userService.fetchUser(any(String.class))).thenReturn(testUser);
        final var tracksWithPhrases = TracksWithPhrasesTestBuilder
                .newBuilder()
                .buildAsList();
        when(combinationMatcher.findCombinationWithMatchingTracks(testSentence, testToken))
                .thenReturn(tracksWithPhrases);

        final var failedCreatePlaylistResponse = PlaylistResponseBuilder
                .newBuilder()
                .withResponseCode(429)
                .build();
        webServer.enqueue(failedCreatePlaylistResponse);
        webServer.enqueue(failedCreatePlaylistResponse);
        webServer.enqueue(failedCreatePlaylistResponse);
        webServer.enqueue(failedCreatePlaylistResponse);
        webServer.enqueue(failedCreatePlaylistResponse);
        webServer.enqueue(failedCreatePlaylistResponse);
        final PlaylistRequest properPlaylistRequest = new PlaylistRequest("TestName", testSentence, testToken);
        assertThrows(TooManyRequestsException.class, () -> service.buildPlaylist(properPlaylistRequest));
    }

    @Test
    void shouldNotThrowTooManyRequestsExceptionWhenCreateNewPlaylistResponseStatusIs429ForFewerThan5Attempts() {
        final var testSentence = "Test Sentence";
        final var testToken = "test-token";
        when(userService.fetchUser(any(String.class))).thenReturn(testUser);
        final var tracksWithPhrases = TracksWithPhrasesTestBuilder
                .newBuilder()
                .buildAsList();
        when(combinationMatcher.findCombinationWithMatchingTracks(testSentence, testToken))
                .thenReturn(tracksWithPhrases);

        final var failedCreatePlaylistResponse = PlaylistResponseBuilder
                .newBuilder()
                .withResponseCode(429)
                .build();
        webServer.enqueue(failedCreatePlaylistResponse);
        webServer.enqueue(failedCreatePlaylistResponse);

        final var successfulCreatePlaylistResponse = PlaylistResponseBuilder
                .newBuilder()
                .withProperPlaylist()
                .build();
        webServer.enqueue(successfulCreatePlaylistResponse);

        final var successfulAddTracksResponse = new MockResponse().setResponseCode(201);
        webServer.enqueue(successfulAddTracksResponse);

        final PlaylistRequest properPlaylistRequest = new PlaylistRequest("TestName", testSentence, testToken);
        assertDoesNotThrow(() -> service.buildPlaylist(properPlaylistRequest));
    }

    @Test
    void shouldThrowTooManyRequestsExceptionWhenAddTracksToPlaylistResponseStatusIs429ForMoreThan5Attempts() {
        final var testSentence = "Test Sentence";
        final var testToken = "test-token";
        when(userService.fetchUser(any(String.class))).thenReturn(testUser);
        final var tracksWithPhrases = TracksWithPhrasesTestBuilder
                .newBuilder()
                .buildAsList();
        when(combinationMatcher.findCombinationWithMatchingTracks(testSentence, testToken))
                .thenReturn(tracksWithPhrases);

        final var successfulNewPlaylistResponse = PlaylistResponseBuilder
                .newBuilder()
                .withProperPlaylist()
                .build();
        webServer.enqueue(successfulNewPlaylistResponse);

        final var failedAddTracksResponse = new MockResponse().setResponseCode(429);
        webServer.enqueue(failedAddTracksResponse);
        webServer.enqueue(failedAddTracksResponse);
        webServer.enqueue(failedAddTracksResponse);
        webServer.enqueue(failedAddTracksResponse);
        webServer.enqueue(failedAddTracksResponse);

        final PlaylistRequest properPlaylistRequest = new PlaylistRequest("TestName", testSentence, testToken);
        assertThrows(TooManyRequestsException.class, () -> service.buildPlaylist(properPlaylistRequest));
    }

    @Test
    void shouldThrowAuthorizationExceptionWhenCreateNewPlaylistResponseStatusIs401() {
        final var testSentence = "Test Sentence";
        final var testToken = "test-token";
        when(userService.fetchUser(any(String.class))).thenReturn(testUser);
        final var tracksWithPhrases = TracksWithPhrasesTestBuilder
                .newBuilder()
                .buildAsList();
        when(combinationMatcher.findCombinationWithMatchingTracks(testSentence, testToken))
                .thenReturn(tracksWithPhrases);

        final var failedCreatePlaylistResponse = PlaylistResponseBuilder
                .newBuilder()
                .withResponseCode(401)
                .build();
        webServer.enqueue(failedCreatePlaylistResponse);
        final PlaylistRequest properPlaylistRequest = new PlaylistRequest("TestName", testSentence, testToken);
        assertThrows(AuthorizationException.class, () -> service.buildPlaylist(properPlaylistRequest));
    }

    @Test
    void shouldThrowAuthorizationExceptionWhenTracksToPlaylistResponseStatusIs401() {
        final var testSentence = "Test Sentence";
        final var testToken = "test-token";
        when(userService.fetchUser(any(String.class))).thenReturn(testUser);
        final var tracksWithPhrases = TracksWithPhrasesTestBuilder
                .newBuilder()
                .buildAsList();
        when(combinationMatcher.findCombinationWithMatchingTracks(testSentence, testToken))
                .thenReturn(tracksWithPhrases);

        final var successfulNewPlaylistResponse = PlaylistResponseBuilder
                .newBuilder()
                .withProperPlaylist()
                .build();
        webServer.enqueue(successfulNewPlaylistResponse);

        final var failedAddTracksResponse = new MockResponse().setResponseCode(401);
        webServer.enqueue(failedAddTracksResponse);

        webServer.enqueue(failedAddTracksResponse);
        final PlaylistRequest properPlaylistRequest = new PlaylistRequest("TestName", testSentence, testToken);
        assertThrows(AuthorizationException.class, () -> service.buildPlaylist(properPlaylistRequest));
    }


}