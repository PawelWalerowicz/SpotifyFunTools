package walerowicz.pawel.SpotifyFun.playlist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import walerowicz.pawel.SpotifyFun.playlist.entities.FoundTracksResultPackage;
import walerowicz.pawel.SpotifyFun.playlist.entities.SearchResult;
import walerowicz.pawel.SpotifyFun.playlist.entities.Track;
import walerowicz.pawel.SpotifyFun.playlist.entities.TracksWithPhrase;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

class ConcurrentSearchCall implements Callable<TracksWithPhrase> {
    private final Logger logger = LoggerFactory.getLogger(ConcurrentSearchCall.class);
    private final String query;
    private final String searchForTrackURL;
    private final HttpEntity<String> httpEntity;
    private final String cleanupRegex;

    ConcurrentSearchCall(final String query,
                         final String searchForTrackURL,
                         final HttpHeaders headerWithToken,
                         final String cleanupRegex
                         ) {
        this.query = query;
        this.searchForTrackURL = searchForTrackURL;
        this.httpEntity = new HttpEntity<>(null, headerWithToken);
        this.cleanupRegex = cleanupRegex;
    }

    @Override
    public TracksWithPhrase call() throws Exception {
        return new TracksWithPhrase(query, searchForTracks());
    }

    private List<Track> searchForTracks() throws URISyntaxException, UnsupportedEncodingException {
        var foundTracksResult = new FoundTracksResultPackage();
        List<Track> matches;
        var counter = 1;
        var initialRequest = true;
        do {
            logger.info("Searching for '{}' ({} attempt)", query, counter);
            final var requestUri = getRequestUri(initialRequest, foundTracksResult);
            foundTracksResult = getResponse(requestUri);
            matches = filterTracks(foundTracksResult);
            counter++;
            initialRequest = false;
        } while (matches.isEmpty() && foundTracksResult.hasNextURL() && counter<10);
        return matches;
    }

    private FoundTracksResultPackage getResponse(final URI uri) {
        return sentTracksRequest(uri).foundTracksResultPackage();
    }

    private SearchResult sentTracksRequest(final URI request) {
        final var restTemplate = new RestTemplate();
        final var responseEntity = restTemplate.exchange(
                                                                            request,
                                                                            HttpMethod.GET,
                                                                            httpEntity,
                                                                            SearchResult.class);
        return responseEntity.getBody();
    }

    private URI getRequestUri(final boolean firstRun,
                              final FoundTracksResultPackage foundTracksResult)
            throws UnsupportedEncodingException, URISyntaxException {
        return firstRun ? buildInitialRequestURI() : foundTracksResult.getNextURL();
    }

    private URI buildInitialRequestURI() throws UnsupportedEncodingException, URISyntaxException {
        return new URI(searchForTrackURL.replace("<QUERY>", encodeQuery()));
    }

    private String encodeQuery() throws UnsupportedEncodingException {
        return URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
    }

    private List<Track> filterTracks(FoundTracksResultPackage foundTracksResult) {
        return foundTracksResult.getFoundTracks().stream()
                .filter(track -> track.name().replaceAll(cleanupRegex, " ").equalsIgnoreCase(query))
                .collect(Collectors.toList());
    }
}