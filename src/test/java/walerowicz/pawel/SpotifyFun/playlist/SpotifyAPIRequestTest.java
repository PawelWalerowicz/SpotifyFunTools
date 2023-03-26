package walerowicz.pawel.SpotifyFun.playlist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import walerowicz.pawel.SpotifyFun.authorization.entites.SpotifyAccessToken;
import walerowicz.pawel.SpotifyFun.authorization.SpotifyAuthorizationService;

import java.net.URI;
import java.net.URISyntaxException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyAPIRequestTest {
    private SpotifyAPIRequest apiRequest;
    @Mock
    private SpotifyAuthorizationService mockService;
    private SpotifyAccessToken testToken;
    private String testTokenString = "testToken";

    @BeforeEach
    void reset() {
        apiRequest = new SpotifyAPIRequest(mockService);
        this.testToken = new SpotifyAccessToken();
        testToken.setToken(testTokenString);
        when(mockService.getAccessToken()).thenReturn(this.testToken).thenReturn(testToken);
    }

    @Test
    void shouldGetAccessTokenBeforeSendingGetRequest() throws URISyntaxException {
        try {
            apiRequest.get(new URI("https://anyUrl:9090"), String.class);
        } catch (ResourceAccessException ignore) {}

        verify(mockService, times(1)).getAccessToken();
    }
    @Test
    void shouldGetAccessTokenBeforeSendingPostRequest() throws URISyntaxException {
        try {
            apiRequest.post(new URI("https://anyUrl:9090"), "test body", String.class);
        } catch (ResourceAccessException ignore) {}

        verify(mockService, times(1)).getAccessToken();
    }

}