package walerowicz.pawel.SpotifyFun.playlist.concurrent.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import walerowicz.pawel.SpotifyFun.authorization.AuthorizationException;
import walerowicz.pawel.SpotifyFun.playlist.entities.FetchTracksResult;
import walerowicz.pawel.SpotifyFun.playlist.entities.FoundTracksResultPackage;
import walerowicz.pawel.SpotifyFun.playlist.entities.Track;
import walerowicz.pawel.SpotifyFun.playlist.entities.TracksWithPhrase;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
class ConcurrentSearch implements Runnable {
    //configuration fields
    private static final String SEARCH_URI = "https://api.spotify.com/v1/search";
    private static final int SINGLE_FETCH_AMOUNT = 50;  //max acceptable by API=50
    private static final int ATTEMPTS_LIMIT = 50;
    private static final int BUFFER_LIMIT_BYTES = 16 * 1024 * 1024;    //16MB
    private static final int WAIT_PERIOD_MS =5000;    //5s

    //constant fields
    private final String query;
    private final String token;
    private final String cleanupRegex;
    private final Set<TracksWithPhrase> outputSet;

    //post-construct fields
    private String encodedQuery;
    private WebClient webClient;

    //state fields
    private boolean shouldShutdown;
    private List<Track> matches;
    private int attemptCounter;

    @Override
    public void run() {
        final var matchingTracks = searchForTracks();
        final var tracksWithPhrase = new TracksWithPhrase(query, matchingTracks);
        outputSet.add(tracksWithPhrase);
    }

    void shutDown() {
        shouldShutdown = true;
    }

    private List<Track> searchForTracks() {
        initState();
        do {
            log.debug("Searching for '{}' ({} attempt)", query, attemptCounter);
            try {
                var fetchTracksResult = fetchTracks();
                filterTracks(fetchTracksResult);
            } catch (TooManyRequestsException e) {
                logAndWait();
            }
            attemptCounter++;
        } while (shouldContinue());
        log.debug("Searching finished for phrase '{}' after {} attempts. Result: {}", query, attemptCounter, matches);
        return matches;
    }

    private void initState() {
        setEncodedQuery();
        initializeWebClient();
        attemptCounter = 1;
        matches = new ArrayList<>();
        shouldShutdown = false;
    }

    private void setEncodedQuery() {
        try {
            this.encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            log.error("Error occurred during encoding of the query", e);
        }
    }

    private void initializeWebClient() {
        this.webClient = WebClient.builder()
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

    private FetchTracksResult fetchTracks() {
        return webClient
                .get()
                .uri(builder -> builder
                        .queryParam("q", encodedQuery)
                        .queryParam("type", "track")
                        .queryParam("offset", calculateOffset())
                        .queryParam("limit", SINGLE_FETCH_AMOUNT)
                        .build()
                )
                .exchangeToMono(response -> {
                    final var httpStatusCode = response.statusCode();
                    if (httpStatusCode.equals(HttpStatus.TOO_MANY_REQUESTS)) {
                        return Mono.error(new TooManyRequestsException());
                    } else if (httpStatusCode.equals(HttpStatus.UNAUTHORIZED)) {
                        return Mono.error(new AuthorizationException("Web token expired"));
                    } else {
                        return response.bodyToMono(FetchTracksResult.class);
                    }
                })
                .block();
    }

    private int calculateOffset() {
        return SINGLE_FETCH_AMOUNT * (attemptCounter - 1);
    }

    private void filterTracks(final FetchTracksResult fetchTracksResult) {
        matches = Optional.ofNullable(fetchTracksResult)
                .map(FetchTracksResult::foundTracksResultPackage)
                .map(FoundTracksResultPackage::foundTracks)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(this::trackNameIsExactMatch)
                .collect(Collectors.toList());
    }

    private boolean trackNameIsExactMatch(final Track track) {
        return StringUtils.stripAccents(track.name()).replaceAll(cleanupRegex, " ").equalsIgnoreCase(query);
    }

    private boolean shouldContinue() {
        return matches.isEmpty()
                && attemptCounter <= ATTEMPTS_LIMIT
                && !shouldShutdown;
    }

    private void logAndWait() {
        log.info("Exceeded request limit for {}, trying again in {} seconds", query, WAIT_PERIOD_MS/1000);
        try {
            Thread.sleep(WAIT_PERIOD_MS);
        } catch (InterruptedException ignored) {
        }
    }
}