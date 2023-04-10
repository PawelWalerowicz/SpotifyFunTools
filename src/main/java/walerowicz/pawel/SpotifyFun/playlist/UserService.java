package walerowicz.pawel.SpotifyFun.playlist;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import walerowicz.pawel.SpotifyFun.authorization.entites.User;

import java.net.URI;
import java.net.URISyntaxException;

@Service
class UserService {
    final SpotifyAPIRequest spotifyAPIRequest;
    private final URI getUserRequestURI;

    UserService(@Value("${spotify.user.profileURL}") final String userProfileURL,
                       final SpotifyAPIRequest spotifyAPIRequest) throws URISyntaxException {
        this.getUserRequestURI = new URI(userProfileURL);
        this.spotifyAPIRequest = spotifyAPIRequest;
    }

    User importUser() {
        return spotifyAPIRequest.get(getUserRequestURI, User.class);
    }
}
