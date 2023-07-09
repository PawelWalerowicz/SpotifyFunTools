package walerowicz.pawel.spotifyfun.authorization.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import walerowicz.pawel.spotifyfun.authorization.entity.SpotifyAccessToken;
import walerowicz.pawel.spotifyfun.authorization.service.SpotifyAuthorizationService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthorizationController.class)
class AuthorizationControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SpotifyAuthorizationService spotifyAuthorizationService;

    @Test
    void shouldReturnRedirectViewAndStatusRedirectionWithAuthorizationCodeURLFetchedFromService() throws Exception {
        when(spotifyAuthorizationService.getAuthorizationCodeURL()).thenReturn("authURL");
        mockMvc.perform(get("/api/v1/auth/login"))
                .andExpect(redirectedUrl("authURL"))
                .andExpect(status().is3xxRedirection());
        verify(spotifyAuthorizationService).getAuthorizationCodeURL();
    }

    @Test
    void shouldReturnSpotifyAccessTokenAndStatusSuccessfulWhenAuthorizationCodeIsProvided() throws Exception {
        final var authorizationCode = "test-code";
        final SpotifyAccessToken expectedAccessToken = new SpotifyAccessToken(
                "test-token",
                "refresh-test-token",
                3600,
                "test-scope");
        when(spotifyAuthorizationService.fetchAccessToken(anyString())).thenReturn(expectedAccessToken);
        final String expectedBody = "{" +
                "\"access_token\":\"test-token\"," +
                "\"refresh_token\":\"refresh-test-token\"," +
                "\"expires_in\":3600," +
                "\"scope\":\"test-scope\"" +
                "}";
        mockMvc.perform(
                        get("/api/v1/auth/token")
                                .param("code", authorizationCode)
                )
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedBody))
                .andExpect(status().is2xxSuccessful());
        verify(spotifyAuthorizationService).fetchAccessToken(authorizationCode);
    }

    @Test
    void shouldReturnProperApiCallProblemAndStatusClientErrorWhenAuthorizationCodeIsNotProvided() throws Exception {
        final String expectedBody = "{" +
                "\"Error message\":\"Required request parameter 'code' for method parameter type String is not present\"" +
                "}";
        mockMvc.perform(
                        get("/api/v1/auth/token")
                )
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedBody))
                .andExpect(status().isBadRequest());
    }
}