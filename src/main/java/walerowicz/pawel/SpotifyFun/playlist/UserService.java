package walerowicz.pawel.SpotifyFun.playlist;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import walerowicz.pawel.SpotifyFun.authorization.User;

import java.net.URI;
import java.net.URISyntaxException;

@Service
class UserService {
    final HTTPRequestWrapper httpRequestWrapper;
    private final String userProfileURL;

    UserService(@Value("${spotify.user.profileURL}") final String userProfileURL,
                       HTTPRequestWrapper httpRequestWrapper) {
        this.userProfileURL = userProfileURL;
        this.httpRequestWrapper = httpRequestWrapper;
    }

    User importUser() throws URISyntaxException {
        URI request = new URI(userProfileURL);
        return httpRequestWrapper.sentGetRequest(request, User.class);
    }
}
