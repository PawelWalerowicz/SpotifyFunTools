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

    @Autowired
    ConcurrentSearchCallFactory(@Value("${spotify.search.track}") String searchForTrackURL,
                                final SpotifyAuthorizationService spotifyAuthorizationService) {
        this.searchForTrackURL = searchForTrackURL;
        this.spotifyAuthorizationService = spotifyAuthorizationService;
    }

    ConcurrentSearchCall createConcurrentSearchCall(final String query) {
        return new ConcurrentSearchCall(query,
                searchForTrackURL,
                getHeader());
    }

    private HttpHeaders getHeader() {
        if (this.header==null) {
            this.header = buildHeader();
        }
        return this.header;
    }

    private HttpHeaders buildHeader() {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.set("Authorization", "Bearer " + spotifyAuthorizationService.getAccessToken().getToken());
        return header;
    }
}
