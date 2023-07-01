package walerowicz.pawel.SpotifyFun.playlist;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import walerowicz.pawel.SpotifyFun.authorization.entites.User;
import walerowicz.pawel.SpotifyFun.playlist.combinations.CombinationMatcher;
import walerowicz.pawel.SpotifyFun.playlist.entities.PlaylistRequest;
import walerowicz.pawel.SpotifyFun.playlist.entities.Track;
import walerowicz.pawel.SpotifyFun.playlist.entities.TracksWithPhrase;
import walerowicz.pawel.SpotifyFun.user.UserService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistGeneratorTest {
    private static MockWebServer webServer;
    @Mock
    private UserService userService;
    @Mock
    private CombinationMatcher combinationMatcher;

    private PlaylistService service;

    @BeforeAll
    static void beforeAll() throws IOException {
        webServer = new MockWebServer();
        webServer.start();
    }

    @BeforeEach
    void reset() {
        final var user = new User("userId");
        when(userService.importUser(any(String.class))).thenReturn(user);
        final var webClient = WebClient.builder()
                .baseUrl(String.format("http://localhost:%s", webServer.getPort()))
                .defaultHeaders(header -> header.setContentType(MediaType.APPLICATION_JSON))
                .build();
        service = new PlaylistService(
                userService,
                combinationMatcher,
                webClient
        );


        final var newPlaylistResponse = new MockResponse()
                .setBody(getResource())
                .addHeader("Content-Type", "application/json")
                .setResponseCode(201);
        webServer.enqueue(newPlaylistResponse);
        final var addItemsResponse = new MockResponse().setResponseCode(201);
        webServer.enqueue(addItemsResponse);
    }

    @AfterAll
    static void afterAll() throws IOException {
        webServer.close();
    }

    @NotNull
    private String getResource() {
        try (final var resourceAsStream = getClass()
                .getClassLoader()
                .getResourceAsStream("playlist_response.json")
        ) {
            final var bytes = Objects.requireNonNull(resourceAsStream).readAllBytes();
            return new String(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            return "Exception while getting test resource";
        }
    }

    @Test
    void shouldCallCombinationMatcherOnceThenSendNewPlaylistRequestsThenSendAddTracksRequest() throws InterruptedException {
        final var testSentence = "Test Sentence";
        final var testToken = "test-token";
        final var tracksWithPhrases = List.of(
                new TracksWithPhrase("Test", List.of(new Track("id1", "href", "name1")))
        );
        when(combinationMatcher.findCombinationWithMatchingTracks(testSentence, testToken)).thenReturn(tracksWithPhrases);

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
        final var tracksWithPhrases = List.of(
                new TracksWithPhrase("Test", List.of(new Track("id1", "href", "name1")))
        );
        when(combinationMatcher.findCombinationWithMatchingTracks(testSentence, testToken)).thenReturn(tracksWithPhrases);

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
        final var tracksWithPhrases = List.of(
                new TracksWithPhrase("Test", List.of(new Track("id1", "href", "name1"))),
                new TracksWithPhrase("Sentence", List.of(new Track("id2", "href", "name2")))
        );
        when(combinationMatcher.findCombinationWithMatchingTracks(testSentence, testToken)).thenReturn(tracksWithPhrases);

        service.buildPlaylist(new PlaylistRequest("TestName", testSentence, testToken));

        webServer.takeRequest();
        final var secondRequest = webServer.takeRequest();
        assertEquals("POST", secondRequest.getMethod());
        assertEquals("Bearer " + testToken, secondRequest.getHeaders().get("Authorization"));
        assertEquals("[\"spotify:track:id1\",\"spotify:track:id2\"]", secondRequest.getBody().readString(StandardCharsets.UTF_8));
    }

    @Test
    void shouldThrowTracksNotFoundExceptionIfCombinationMatcherReturnsEmptyList() {
        final var testSentence = "Test Sentence";
        final var testToken = "test-token";
        when(combinationMatcher.findCombinationWithMatchingTracks(testSentence, testToken))
                .thenReturn(Collections.emptyList());

        assertThrows(TracksNotFoundException.class, () ->
                service.buildPlaylist(new PlaylistRequest("TestName", testSentence, testToken))
        );
    }

    @Test
    void shouldChoseSingleRandomTrackForEachPhraseFromReturnedList() throws InterruptedException {
        final var testSentence = "Test Sentence";
        final var testToken = "test-token";
        final List<Track> phrase1Tracks = List.of(
                new Track("id11", "href11", "name11"),
                new Track("id12", "href12", "name12"),
                new Track("id13", "href13", "name13")
                );
        final TracksWithPhrase phrase1 = new TracksWithPhrase("Phrase1", phrase1Tracks);
        final List<Track> phrase2Tracks = List.of(
                new Track("id21", "href21", "name21"),
                new Track("id22", "href22", "name22"),
                new Track("id23", "href23", "name23")
                );
        final TracksWithPhrase phrase2 = new TracksWithPhrase("Phrase2", phrase2Tracks);
        final var tracksWithPhrases = List.of(phrase1, phrase2);
        when(combinationMatcher.findCombinationWithMatchingTracks(testSentence, testToken)).thenReturn(tracksWithPhrases);


        service.buildPlaylist(new PlaylistRequest("TestName", testSentence, testToken));

        webServer.takeRequest();
        final var secondRequestBody = webServer.takeRequest().getBody().readString(StandardCharsets.UTF_8);
        final List<String> singleTrackRequests = Arrays.stream(secondRequestBody
                .replaceAll("[\\[\\]\"]", "")
                .split(",")
        ).toList();
        assertTrue(singleTrackRequests.get(0).matches("^spotify:track:id1\\d$"));
        assertTrue(singleTrackRequests.get(1).matches("^spotify:track:id2\\d$"));
        assertEquals(2, singleTrackRequests.size());
    }

}