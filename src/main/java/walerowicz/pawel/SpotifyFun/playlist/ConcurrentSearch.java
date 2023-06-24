package walerowicz.pawel.SpotifyFun.playlist;

import lombok.extern.slf4j.Slf4j;
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
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
class ConcurrentSearch implements Runnable {
    private final String query;
    private final String searchForTrackURL;
    private final String cleanupRegex;
    private final String token;
    private final Set<TracksWithPhrase> outputSet;

    ConcurrentSearch(final String query,
                     final String searchForTrackURL,
                     final String cleanupRegex,
                     final String token,
                     final Set<TracksWithPhrase> outputSet
    ) {
        this.query = query;
        this.searchForTrackURL = searchForTrackURL;
        this.cleanupRegex = cleanupRegex;
        this.token = token;
        this.outputSet = outputSet;
    }

    @Override
    public void run() {
        outputSet.add(new TracksWithPhrase(query, searchForTracks()));
    }

    private List<Track> searchForTracks() {
        final var currentThread = Thread.currentThread();
        var foundTracksResult = new FoundTracksResultPackage();
        List<Track> matches;
        var attemptCounter = 1;
        var initialRequest = true;
        do {
            log.debug("Searching for '{}' ({} attempt)", query, attemptCounter);
            final var requestUri = getRequestUri(initialRequest, foundTracksResult);
            foundTracksResult = getResponse(requestUri);
            matches = filterTracks(foundTracksResult);
            attemptCounter++;
            initialRequest = false;
        } while (matches.isEmpty() && foundTracksResult.hasNextURL() && attemptCounter < 50 && !currentThread.isInterrupted());
        log.debug("Searching finished for phrase '{}' after {} attempts. Result: {}", query, attemptCounter, matches);
        return matches;
    }

    private FoundTracksResultPackage getResponse(final URI uri) {
        return spotifyAPIRequest.get(uri, SearchResult.class).foundTracksResultPackage();
    }

    private URI getRequestUri(final boolean firstRun,
                              final FoundTracksResultPackage foundTracksResult) {
        return firstRun ? buildInitialRequestURI() : foundTracksResult.getNextURL();
    }

    private URI buildInitialRequestURI() {
        try {
            return new URI(searchForTrackURL.replace("<QUERY>", encodeQuery()));
        } catch (URISyntaxException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
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