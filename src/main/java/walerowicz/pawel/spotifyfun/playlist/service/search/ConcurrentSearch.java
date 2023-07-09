package walerowicz.pawel.spotifyfun.playlist.service.search;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import walerowicz.pawel.spotifyfun.playlist.entity.FetchTracksResult;
import walerowicz.pawel.spotifyfun.playlist.entity.FoundTracksResultPackage;
import walerowicz.pawel.spotifyfun.playlist.entity.Track;
import walerowicz.pawel.spotifyfun.playlist.entity.TracksWithPhrase;
import walerowicz.pawel.spotifyfun.playlist.exception.TooManyRequestsException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
@Slf4j
class ConcurrentSearch implements Runnable {
    //configuration fields
    private static final int SINGLE_FETCH_AMOUNT = 50;  //max acceptable by API=50
    private static final int BUFFER_LIMIT_BYTES = 16 * 1024 * 1024;    //16MB

    //constant fields
    private final String query;
    private final String searchUrl;
    private final String cleanupRegex;
    private final String token;
    private final Set<TracksWithPhrase> outputSet;
    private final int attemptsLimit;
    private final int waitPeriodMs;

    //post-construct fields
    private String encodedQuery;
    private WebClient webClient;

    //state fields
    private boolean isRunning;
    private List<Track> matches;
    private int attemptCounter;

    @Override
    public void run() {
        final var matchingTracks = searchForTracks();
        final var tracksWithPhrase = new TracksWithPhrase(query, matchingTracks);
        outputSet.add(tracksWithPhrase);
    }

    void shutDown() {
        isRunning = false;
        Thread.currentThread().interrupt();
    }

    boolean isRunning() {
        return isRunning;
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
        isRunning = true;
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
                .baseUrl(searchUrl)
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
                        log.info("Web token expired - requests stopped.");
                        shutDown();
                        return Mono.empty();
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
                .toList();
    }

    private boolean trackNameIsExactMatch(final Track track) {
        return StringUtils.stripAccents(track.name()).replaceAll(cleanupRegex, " ").equalsIgnoreCase(query);
    }

    private boolean shouldContinue() {
        return matches.isEmpty()
                && attemptCounter <= attemptsLimit
                && isRunning;
    }

    private void logAndWait() {
        log.info("Exceeded request limit for {}, trying again in {} seconds", query, waitPeriodMs / 1000);
        Sleeper.sleep(waitPeriodMs);
    }
}