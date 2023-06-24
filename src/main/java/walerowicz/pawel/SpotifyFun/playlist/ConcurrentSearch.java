package walerowicz.pawel.SpotifyFun.playlist;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import walerowicz.pawel.SpotifyFun.playlist.entities.FoundTracksResultPackage;
import walerowicz.pawel.SpotifyFun.playlist.entities.SearchResult;
import walerowicz.pawel.SpotifyFun.playlist.entities.Track;
import walerowicz.pawel.SpotifyFun.playlist.entities.TracksWithPhrase;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
class ConcurrentSearch implements Runnable {
    private final String query;
    private final String cleanupRegex;
    private final Set<TracksWithPhrase> outputSet;
    private final WebClient webClient;
    private static final String SEARCH_URI = "https://api.spotify.com/v1/search";
    private final int SINGLE_FETCH_AMOUNT = 50;
    private final int ATTEMPTS_LIMIT=50;
    private final int BUFFER_LIMIT_BYTES = 16 * 1024 * 1024;
    private boolean shouldContinue;

    ConcurrentSearch(final String query,
                     final String cleanupRegex,
                     final String token,
                     final Set<TracksWithPhrase> outputSet
    ) {
        this.query = query;
        this.cleanupRegex = cleanupRegex;
        this.outputSet = outputSet;
        this.webClient = initializeWebClient(token);
        this.shouldContinue = true;
    }

    void shutDown() {
        shouldContinue = false;
    }

    @Override
    public void run() {
        outputSet.add(new TracksWithPhrase(query, searchForTracks()));
    }

    private WebClient initializeWebClient(final String token) {
        return WebClient.builder()
                .baseUrl(SEARCH_URI)
                .defaultHeaders(header -> {
                    header.setContentType(MediaType.APPLICATION_JSON);
                    header.setBearerAuth(token);
                })
                .codecs(config -> config
                        .defaultCodecs()
                        .maxInMemorySize(BUFFER_LIMIT_BYTES))
                .build();
    }

    private List<Track> searchForTracks() {
        var foundTracksResult = new FoundTracksResultPackage();
        List<Track> matches;
        var attemptCounter = 1;
        do {
            log.debug("Searching for '{}' ({} attempt)", query, attemptCounter);
            foundTracksResult = search(attemptCounter).foundTracksResultPackage();
            matches = filterTracks(foundTracksResult);
            attemptCounter++;
        } while (matches.isEmpty() && attemptCounter <= ATTEMPTS_LIMIT && shouldContinue);
        log.debug("Searching finished for phrase '{}' after {} attempts. Result: {}", query, attemptCounter, matches);
        return matches;
    }

    private SearchResult search(final int attempt) {
        return webClient
                .get()
                .uri(builder -> builder
                        .queryParam("q", encodeQuery())
                        .queryParam("type", "track")
                        .queryParam("offset", SINGLE_FETCH_AMOUNT*(attempt-1))
                        .queryParam("limit", SINGLE_FETCH_AMOUNT)
                        .build()
                )
                .retrieve()
                .bodyToMono(SearchResult.class)
                .block();
    }

    private String encodeQuery() {
        try {
            return URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Track> filterTracks(FoundTracksResultPackage foundTracksResult) {
        return foundTracksResult.getFoundTracks().stream()
                .filter(track -> track.name().replaceAll(cleanupRegex, " ").equalsIgnoreCase(query))
                .collect(Collectors.toList());
    }
}