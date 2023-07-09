package walerowicz.pawel.spotifyfun.playlist.service.search;

import lombok.RequiredArgsConstructor;
import walerowicz.pawel.spotifyfun.playlist.entity.TracksWithPhrase;

import java.util.Set;

@RequiredArgsConstructor
public class ConcurrentSearchFactory {
    private static final String SEARCH_URI = "https://api.spotify.com/v1/search";
    private static final int ATTEMPTS_LIMIT = 50;
    private static final int WAIT_PERIOD_MS = 5000;

    private final String cleanupRegex;
    private final String token;
    private final Set<TracksWithPhrase> outputSet;


    public ConcurrentSearch buildConcurrentSearch(final String query) {
        return new ConcurrentSearch(
                query,
                SEARCH_URI,
                cleanupRegex,
                token,
                outputSet,
                ATTEMPTS_LIMIT,
                WAIT_PERIOD_MS
        );
    }
}
