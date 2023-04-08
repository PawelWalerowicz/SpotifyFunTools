package walerowicz.pawel.SpotifyFun.playlist;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import walerowicz.pawel.SpotifyFun.playlist.entities.ExternalUrls;
import walerowicz.pawel.SpotifyFun.playlist.entities.Playlist;
import walerowicz.pawel.SpotifyFun.playlist.entities.PlaylistUrl;
import walerowicz.pawel.SpotifyFun.playlist.entities.TracksWithPhrase;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
class PlaylistGenerator {
    private static final Logger logger = LoggerFactory.getLogger(PlaylistGenerator.class);
    private final SpotifyAPIRequest spotifyAPIRequest;
    private final UserService userService;
    private final CombinationMatcher combinationMatcher;
    private final String createPlaylistURL;
    private final String addItemToPlaylistURL;

    @Autowired
    public PlaylistGenerator(final SpotifyAPIRequest spotifyAPIRequest,
                             final UserService userService,
                             final CombinationMatcher combinationMatcher,
                             @Value("${spotify.playlist.create}") final String createPlaylistURL,
                             @Value("${spotify.playlist.item.add}") final String addItemToPlaylistURL) {
        this.spotifyAPIRequest = spotifyAPIRequest;
        this.userService = userService;
        this.combinationMatcher = combinationMatcher;
        this.createPlaylistURL = createPlaylistURL;
        this.addItemToPlaylistURL = addItemToPlaylistURL;
    }

    PlaylistUrl buildPlaylist(final String playlistName, final String inputSentence)
            throws URISyntaxException, JsonProcessingException, CombinationNotFoundException {
        final var start = Instant.now();
        logger.info("Creating playlist '{}' from sentence '{}'", playlistName, inputSentence);
        final var combinationTracks = combinationMatcher.findCombinationWithMatchingTracks(inputSentence);
        final var playlist = createNewPlaylist(playlistName);
        final var end = Instant.now();
        double timeDifference = ((double) Duration.between(start, end).toMillis()) / 1000;
        logger.info("Found combination in {} seconds", timeDifference);
        addToPlaylist(playlist, combinationTracks);
        return new PlaylistUrl(playlist.externalUrls().url());
    }

    private Playlist createNewPlaylist(final String playlistName) throws URISyntaxException {
        final var user = userService.importUser();
        final var request = new URI(createPlaylistURL.replace("<USER_ID>", user.id()));
        final var body = "{\"name\": \"" + playlistName + "\"}";
        return spotifyAPIRequest.post(request, body, Playlist.class);
    }

    private void addToPlaylist(final Playlist playlist, final List<TracksWithPhrase> tracks)
            throws URISyntaxException, JsonProcessingException {
        final var finalTracks = chooseRandomMatchingTracks(tracks);
        final var objectMapper = new ObjectMapper();
        final var body = objectMapper.writeValueAsString(finalTracks);
        final var request = new URI(addItemToPlaylistURL.replace("<PLAYLIST_ID>", playlist.id()));
        spotifyAPIRequest.post(request, body, String.class);
    }

    private List<String> chooseRandomMatchingTracks(final List<TracksWithPhrase> tracks) {
        final var random = new Random();
        return tracks.stream()
                .map(TracksWithPhrase::matchingTracks)
                .filter(trackList -> trackList.size() > 0)
                .map(trackList -> "spotify:track:" + trackList.get(random.nextInt(trackList.size())).id())
                .collect(Collectors.toList());
    }


}