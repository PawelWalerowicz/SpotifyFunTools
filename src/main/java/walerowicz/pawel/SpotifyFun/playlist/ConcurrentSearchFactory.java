package walerowicz.pawel.SpotifyFun.playlist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import walerowicz.pawel.SpotifyFun.authorization.SpotifyAuthorizationService;
import walerowicz.pawel.SpotifyFun.playlist.entities.TracksWithPhrase;

import java.util.Set;

@Service
class ConcurrentSearchFactory {
    private final String searchForTrackURL;
    private final String cleanupRegex;
    private final SpotifyAuthorizationService spotifyAuthorizationService;
    private final RetryPolicyConfiguration retryPolicyConfiguration;

    @Autowired
    ConcurrentSearchFactory(@Value("${spotify.uri.search.track}") final String searchForTrackURL,
                            @Value("${spotify.combinator.cleanup.regex}") final String cleanupRegex,
                            final SpotifyAuthorizationService spotifyAuthorizationService,
                            final RetryPolicyConfiguration retryPolicyConfiguration) {
        this.searchForTrackURL = searchForTrackURL;
        this.cleanupRegex = cleanupRegex;
        this.spotifyAuthorizationService = spotifyAuthorizationService;
        this.retryPolicyConfiguration = retryPolicyConfiguration;
    }

    ConcurrentSearch createConcurrentSearchInstance(final String query, final Set<TracksWithPhrase> outputSet) {
        return new ConcurrentSearch(
                query,
                searchForTrackURL,
                cleanupRegex,
                token,
                outputSet
        );
    }
}