package walerowicz.pawel.SpotifyFun.playlist;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import walerowicz.pawel.SpotifyFun.authorization.entites.User;
import walerowicz.pawel.SpotifyFun.playlist.entities.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistGeneratorTest {
    @Mock
    private SpotifyAPIRequest spotifyAPIRequest;
    @Mock
    private UserService userService;
    private final User user = new User("userId");
    @Mock
    private CombinationMatcher combinationMatcher;
    private final String createPlaylistURL = "createPlaylist-<USER_ID>-URL";
    private final String addItemToPlaylistURL = "addItemToPlaylist-<PLAYLIST_ID>-URL";

    private PlaylistGenerator generator;
    @BeforeEach
    void reset() {
        when(userService.importUser()).thenReturn(user);

        generator = new PlaylistGenerator(
                spotifyAPIRequest,
                userService,
                combinationMatcher,
                createPlaylistURL,
                addItemToPlaylistURL
        );
    }

    private List<TracksWithPhrase> getTestTracks(final String inputSentence) {
        return List.of(
                new TracksWithPhrase(
                        inputSentence,
                        List.of(
                                new Track(
                                        "trackId",
                                        "href",
                                        "TrackName"
                                )
                        )
                )
        );
    }

    @Test
    void shouldReplaceUSER_IDInRequestURLWithProperId() throws URISyntaxException, JsonProcessingException {
        final var inputSentence = "WordA WordB";
        final var playlistTitle = "TestName";
        final var expectedRequestURL = new URI("createPlaylist-userId-URL");
        final var expectedBodyJson =  "{\"name\": \"" + playlistTitle + "\"}";
        when(combinationMatcher.findCombinationWithMatchingTracks(inputSentence))
                .thenReturn(getTestTracks(inputSentence));
        when(spotifyAPIRequest.post(expectedRequestURL, expectedBodyJson, Playlist.class))
                .thenReturn(new Playlist(new ExternalUrls("externalURL"), "href", "playlistId"));

        generator.buildPlaylist(playlistTitle, inputSentence);

        verify(spotifyAPIRequest, times(1))
                .post(expectedRequestURL, expectedBodyJson, Playlist.class);
    }

    @Test
    void shouldReplacePLAYLIST_IDInAddItemToPlaylistURLWithProperId() throws URISyntaxException, JsonProcessingException {
        final var inputSentence = "WordA WordB";
        final var playlistTitle = "TestName";
        final var expectedCreatePlaylistURL = new URI("createPlaylist-userId-URL");
        final var expectedCreatePlaylistBodyJson =  "{\"name\": \"" + playlistTitle + "\"}";
        final var expectedAddItemURL = new URI("addItemToPlaylist-playlistId-URL");
        final var expectedAddItemBodyJson = "[\"spotify:track:trackId\"]";
        when(combinationMatcher.findCombinationWithMatchingTracks(any(String.class)))
                .thenReturn(getTestTracks(inputSentence));
        when(spotifyAPIRequest.post(expectedCreatePlaylistURL, expectedCreatePlaylistBodyJson, Playlist.class))
                .thenReturn(new Playlist(new ExternalUrls("externalURL"), "href", "playlistId"));

        generator.buildPlaylist(playlistTitle, inputSentence);

        verify(spotifyAPIRequest, times(1))
                .post(expectedAddItemURL, expectedAddItemBodyJson, String.class);
    }

//    @Test
//    void shouldChoseRandomTrackFromMatchingTracks() {
//
//    }

}