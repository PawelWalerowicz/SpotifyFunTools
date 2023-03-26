package walerowicz.pawel.SpotifyFun.playlist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import walerowicz.pawel.SpotifyFun.authorization.entites.User;

import java.net.URI;
import java.net.URISyntaxException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    private UserService userService;
    private final String mockUserProfileUrl = "http://mockUrl:9090";
    @Mock
    private SpotifyAPIRequest mockRequest;

    @BeforeEach
    void reset() throws URISyntaxException {
        userService = new UserService(mockUserProfileUrl, mockRequest);
    }

    @Test
    void shouldSendSpotifyApiRequestForGivenUrl() throws URISyntaxException {
        final User testUser = new User("Test user id");
        when(mockRequest.get(new URI(mockUserProfileUrl), User.class)).thenReturn(testUser);
        userService.importUser();
        verify(mockRequest, times(1)).get(new URI(mockUserProfileUrl), User.class);
    }


}