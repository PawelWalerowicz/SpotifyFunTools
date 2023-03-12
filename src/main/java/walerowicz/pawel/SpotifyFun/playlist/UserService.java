package walerowicz.pawel.SpotifyFun.playlist;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import walerowicz.pawel.SpotifyFun.authorization.User;

import java.net.URI;
import java.net.URISyntaxException;

@Service
class UserService {
    final SpotifyAPIRequest spotifyAPIRequest;
    private final String userProfileURL;

    UserService(@Value("${spotify.user.profileURL}") final String userProfileURL,
                       SpotifyAPIRequest spotifyAPIRequest) {
        this.userProfileURL = userProfileURL;
        this.spotifyAPIRequest = spotifyAPIRequest;
    }

    User importUser() throws URISyntaxException {
        URI getUserRequestURI = new URI(userProfileURL);
        return spotifyAPIRequest.get(getUserRequestURI, User.class);
    }
}
