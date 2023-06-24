package walerowicz.pawel.SpotifyFun.playlist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import walerowicz.pawel.SpotifyFun.authorization.entites.User;
import walerowicz.pawel.SpotifyFun.playlist.entities.Playlist;
import walerowicz.pawel.SpotifyFun.playlist.entities.PlaylistRequest;
import walerowicz.pawel.SpotifyFun.playlist.entities.PlaylistUrl;
import walerowicz.pawel.SpotifyFun.playlist.entities.TracksWithPhrase;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
class PlaylistGenerator {
    private static final String CREATE_PLAYLIST_URI = "users/{userId}/playlists";
    private static final String ADD_ITEM_TO_PLAYLIST_URI = "playlists/{playlistId}/tracks";

    private final UserService userService;
    private final CombinationMatcher combinationMatcher;
    private final WebClient webClient;

    PlaylistUrl buildPlaylist(final PlaylistRequest request) {
        final var start = Instant.now();
        final var playlistName = request.name();
        final var inputSentence = request.sentence();
        final var token = request.token();
        final var user = userService.importUser(token);
        log.info("Creating playlist '{}' from sentence '{}'", playlistName, inputSentence);
        final var combinationTracks = combinationMatcher.findCombinationWithMatchingTracks(inputSentence, token);
        final var playlist = createNewPlaylist(playlistName, token, user);
        logExecutionTime(start);
        final var finalTracks = chooseRandomMatchingTracks(combinationTracks);
        fillToPlaylist(playlist, finalTracks, token);
        return new PlaylistUrl(playlist.externalUrls().url());
    }

    private Playlist createNewPlaylist(final String playlistName,
                                       final String token,
                                       final User user) {
        return webClient
                .post()
                .uri(builder -> builder.path(CREATE_PLAYLIST_URI).build(user.id()))
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .bodyValue(Collections.singletonMap("name", playlistName))
                .retrieve()
                .bodyToMono(Playlist.class)
                .block();
    }

    private void fillToPlaylist(final Playlist playlist,
                                final List<String> finalTracks,
                                final String token) {
        webClient
                .post()
                .uri(builder -> builder.path(ADD_ITEM_TO_PLAYLIST_URI).build(playlist.id()))
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .bodyValue(finalTracks)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    private List<String> chooseRandomMatchingTracks(final List<TracksWithPhrase> tracks) {
        final var random = new Random();
        return tracks.stream()
                .map(TracksWithPhrase::matchingTracks)
                .filter(trackList -> trackList.size() > 0)
                .map(trackList -> "spotify:track:" + trackList.get(random.nextInt(trackList.size())).id())
                .collect(Collectors.toList());
    }

    private void logExecutionTime(final Instant start) {
        final var end = Instant.now();
        final var timeDifference = ((double) Duration.between(start, end).toMillis()) / 1000;
        log.info("Found combination in {} seconds", timeDifference);
    }
}