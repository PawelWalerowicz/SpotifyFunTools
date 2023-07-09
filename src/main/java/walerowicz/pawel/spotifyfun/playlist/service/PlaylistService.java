package walerowicz.pawel.spotifyfun.playlist.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import walerowicz.pawel.spotifyfun.authorization.exception.AuthorizationException;
import walerowicz.pawel.spotifyfun.authorization.service.UserService;
import walerowicz.pawel.spotifyfun.playlist.entity.Playlist;
import walerowicz.pawel.spotifyfun.playlist.entity.PlaylistRequest;
import walerowicz.pawel.spotifyfun.playlist.entity.PlaylistUrl;
import walerowicz.pawel.spotifyfun.playlist.entity.TracksWithPhrase;
import walerowicz.pawel.spotifyfun.playlist.exception.TooManyRequestsException;
import walerowicz.pawel.spotifyfun.playlist.exception.TracksNotFoundException;
import walerowicz.pawel.spotifyfun.playlist.service.combinations.CombinationMatcher;
import walerowicz.pawel.spotifyfun.playlist.service.search.Sleeper;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaylistService {
    private static final String CREATE_PLAYLIST_URI = "users/{userId}/playlists";
    private static final String ADD_ITEM_TO_PLAYLIST_URI = "playlists/{playlistId}/tracks";
    private static final int PLAYLIST_CREATION_RETRIES = 5;
    private static final Random RANDOM_INSTANCE = new Random();
    private final UserService userService;
    private final CombinationMatcher combinationMatcher;
    private final WebClient webClient;
    @Value("${spotify.retry_wait_time_ms}")
    private final int waitTimeBetweenRetriesMs;

    public PlaylistUrl buildPlaylist(final PlaylistRequest request) {
        final var start = Instant.now();
        final var matchingTracks = getMatchingTracks(request);
        final var playlist = createAndPopulateNewPlaylistWithRetries(request, matchingTracks);
        logExecutionTime(start);
        return new PlaylistUrl(playlist.getPlaylistUrl());
    }

    private List<String> getMatchingTracks(final PlaylistRequest request) {
        final var playlistName = request.name();
        final var inputSentence = request.sentence();
        final var token = request.token();
        log.info("Creating playlist '{}' from sentence '{}'", playlistName, inputSentence);
        final var combinationTracks = combinationMatcher.findCombinationWithMatchingTracks(inputSentence, token);
        if (combinationTracks.isEmpty()) {
            throw new TracksNotFoundException("Couldn't find tracks for given input sentence");
        } else {
            return chooseRandomMatchingTracks(combinationTracks);
        }
    }

    private Playlist createAndPopulateNewPlaylistWithRetries(final PlaylistRequest request,
                                                             final List<String> chosenTracks) {
        int attempt = 1;
        do {
            try {
                return createAndPopulateNewPlaylist(request, chosenTracks);
            } catch (TooManyRequestsException e) {
                log.warn("TooManyRequestsException occurred during creation or adding tracks to playlist, will retry.");
                Sleeper.sleep(waitTimeBetweenRetriesMs);
                attempt++;
            }
        } while (attempt < PLAYLIST_CREATION_RETRIES);
        throw new TooManyRequestsException();
    }

    private Playlist createAndPopulateNewPlaylist(final PlaylistRequest request,
                                                  final List<String> chosenTracks) {
        final var token = request.token();
        final var playlist = createNewPlaylist(request);
        addTracksToPlaylist(playlist, chosenTracks, token);
        return playlist;
    }

    private Playlist createNewPlaylist(final PlaylistRequest request) {
        final var playlistName = request.name();
        final var token = request.token();
        final var user = userService.fetchUser(token);
        return webClient
                .post()
                .uri(builder -> builder.path(CREATE_PLAYLIST_URI).build(user.id()))
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .bodyValue(Collections.singletonMap("name", playlistName))
                .exchangeToMono(handleResponse(Playlist.class))
                .block();
    }

    private void addTracksToPlaylist(final Playlist playlist,
                                     final List<String> finalTracks,
                                     final String token) {
        webClient
                .post()
                .uri(builder -> builder.path(ADD_ITEM_TO_PLAYLIST_URI).build(playlist.id()))
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .bodyValue(finalTracks)
                .exchangeToMono(handleResponse(Void.class))
                .block();
    }

    private <T> Function<ClientResponse, Mono<T>> handleResponse(Class<T> outputClass) {
        return response -> {
            final var httpStatusCode = response.statusCode();
            if (httpStatusCode.equals(HttpStatus.TOO_MANY_REQUESTS)) {
                return Mono.error(new TooManyRequestsException());
            } else if (httpStatusCode.equals(HttpStatus.UNAUTHORIZED)) {
                return Mono.error(new AuthorizationException("Web token expired"));
            } else {
                return response.bodyToMono(outputClass);
            }
        };
    }

    private List<String> chooseRandomMatchingTracks(final List<TracksWithPhrase> tracks) {
        return tracks.stream()
                .map(TracksWithPhrase::matchingTracks)
                .filter(trackList -> !trackList.isEmpty())
                .map(trackList -> "spotify:track:" + trackList.get(RANDOM_INSTANCE.nextInt(trackList.size())).id())
                .toList();
    }

    private void logExecutionTime(final Instant start) {
        final var end = Instant.now();
        final var timeDifference = ((double) Duration.between(start, end).toMillis()) / 1000;
        log.info("Found combination in {} seconds", timeDifference);
    }
}