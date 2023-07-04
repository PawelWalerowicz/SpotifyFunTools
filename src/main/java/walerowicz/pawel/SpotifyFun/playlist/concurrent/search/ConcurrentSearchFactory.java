package walerowicz.pawel.SpotifyFun.playlist.concurrent.search;

import lombok.RequiredArgsConstructor;
import walerowicz.pawel.SpotifyFun.playlist.entities.TracksWithPhrase;

import java.util.Set;

@RequiredArgsConstructor
public class ConcurrentSearchFactory {
    private static final String SEARCH_URI = "https://api.spotify.com/v1/search";
    private final int ATTEMPTS_LIMIT = 50;
    private final int WAIT_PERIOD_MS = 5000;

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
                WAIT_PERIOD_MS,
                new Sleeper()
        );
    }
}
