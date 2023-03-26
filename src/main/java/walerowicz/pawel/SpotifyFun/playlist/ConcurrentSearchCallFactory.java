package walerowicz.pawel.SpotifyFun.playlist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import walerowicz.pawel.SpotifyFun.authorization.SpotifyAuthorizationService;

@Service
class ConcurrentSearchCallFactory {
    private final String searchForTrackURL;
    private final SpotifyAuthorizationService spotifyAuthorizationService;
    private HttpHeaders header;
    private final String cleanupRegex;

    @Autowired
    ConcurrentSearchCallFactory(@Value("${spotify.search.track}") final String searchForTrackURL,
                                final SpotifyAuthorizationService spotifyAuthorizationService,
                                @Value("${spotify.combinator.cleanup.regex}") final String cleanupRegex) {
        this.searchForTrackURL = searchForTrackURL;
        this.spotifyAuthorizationService = spotifyAuthorizationService;
        this.cleanupRegex = cleanupRegex;
    }

    ConcurrentSearchCall createConcurrentSearchCall(final String query) {
        return new ConcurrentSearchCall(
                query,
                searchForTrackURL,
                getHeader(),
                cleanupRegex
        );
    }

    private HttpHeaders getHeader() {
        if (this.header==null) {
            this.header = buildHeader();
        }
        return this.header;
    }

    private HttpHeaders buildHeader() {
        var header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.set("Authorization", "Bearer " + spotifyAuthorizationService.getAccessToken().getToken());
        return header;
    }
}
