package walerowicz.pawel.SpotifyFun.playlist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import walerowicz.pawel.SpotifyFun.playlist.entities.FoundTracksResultPackage;
import walerowicz.pawel.SpotifyFun.playlist.entities.SearchResult;
import walerowicz.pawel.SpotifyFun.playlist.entities.Track;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

class ConcurrentSearchCall implements Callable<Map<String, List<Track>>> {
    private final Logger logger = LoggerFactory.getLogger(ConcurrentSearchCall.class);
    private final String query;
    private final String searchForTrackURL;
    final HttpEntity<String> httpEntity;

    ConcurrentSearchCall(final String query, final String searchForTrackURL, final HttpHeaders headerWithToken) {
        this.query = query;
        this.searchForTrackURL = searchForTrackURL;
        this.httpEntity = new HttpEntity<>(null, headerWithToken);
    }

    @Override
    public Map<String, List<Track>> call() throws Exception {
        return Collections.singletonMap(query, searchForTracks());
    }

    private List<Track> searchForTracks() throws URISyntaxException, UnsupportedEncodingException {
        FoundTracksResultPackage foundTracksResult = new FoundTracksResultPackage();
        List<Track> matches;
        int counter = 1;
        boolean initialRequest = true;
        do {
            logger.info("Searching for '{}' ({} attempt)", query, counter);
            URI requestUri = getRequestUri(initialRequest, foundTracksResult);
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
        final RestTemplate restTemplate = new RestTemplate();
        final ResponseEntity<SearchResult> responseEntity = restTemplate.exchange(request, HttpMethod.GET, httpEntity, SearchResult.class);
        return responseEntity.getBody();
    }

    private URI getRequestUri(final boolean firstRun, FoundTracksResultPackage foundTracksResult) throws UnsupportedEncodingException, URISyntaxException {
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
                .filter(track -> track.name().equalsIgnoreCase(query))
                .collect(Collectors.toList());
    }

}