package walerowicz.pawel.spotifyfun.playlist.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import walerowicz.pawel.spotifyfun.authorization.exception.AuthorizationException;
import walerowicz.pawel.spotifyfun.playlist.entity.PlaylistRequest;
import walerowicz.pawel.spotifyfun.playlist.entity.PlaylistUrl;
import walerowicz.pawel.spotifyfun.playlist.exception.TooManyRequestsException;
import walerowicz.pawel.spotifyfun.playlist.exception.TracksNotFoundException;
import walerowicz.pawel.spotifyfun.playlist.service.PlaylistService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PlaylistController.class)
class PlaylistControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PlaylistService playlistService;

    @Test
    void shouldReturnRedirectViewAndStatusRedirectionWithAuthorizationCodeURLFetchedFromService() throws Exception {
        final var properPlaylistRequest = new PlaylistRequest("test-name", "Input sentence", "test-token");
        final var expectedResult = new PlaylistUrl("result-url");
        final var requestBody = "{" +
                "\"name\":\"test-name\"," +
                "\"sentence\":\"Input sentence\"," +
                "\"token\":\"test-token\"" +
                "}";
        final var resultBody = "{\"playlist\":\"result-url\"}";
        when(playlistService.buildPlaylist(properPlaylistRequest)).thenReturn(expectedResult);
        mockMvc.perform(
                        post("/api/v1/playlist/new")
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(content().json(resultBody))
                .andExpect(status().isCreated());
        verify(playlistService).buildPlaylist(properPlaylistRequest);
    }

    @Test
    void shouldReturnApiCallProblemWithStatusBadRequestAndAllErrorsInRequestBody() throws Exception {
        final var requestBody = "{" +
                "\"name\":\"\"," +  //empty playlist name
                "\"sentence\": \"" +
                "Input sentence".repeat(100) +  //too long input sentence
                "\"}";  //missing request token
        final var resultBody = "[" +
                "{\"Error message\":\"Valid request token is required, please visit api/v1/auth/login to acquire one\"}," +
                "{\"Error message\":\"Playlist name can't be empty\"}," +
                "{\"Error message\":\"Request sentence can't be longer than 500 characters\"}" +
                "]";
        mockMvc.perform(
                        post("/api/v1/playlist/new")
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(content().json(resultBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnApiCallProblemWithStatusBadRequestAndInformationAboutMissingBody() throws Exception {
        final var resultBody = "{\"Error message\":\"Request must contain a body with playlist name, sentence to transform and valid authorization token.\"}";
        mockMvc.perform(
                        post("/api/v1/playlist/new")
                                .contentType("application/json")
                )
                .andExpect(content().json(resultBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnApiCallProblemWithStatusUnauthorizedAndProperMessageWhenTokenIsExpired() throws Exception {
        when(playlistService.buildPlaylist(any(PlaylistRequest.class))).thenThrow(new AuthorizationException("Web token expired"));
        final var requestBody = "{" +
                "\"name\":\"test-name\"," +
                "\"sentence\":\"Input sentence\"," +
                "\"token\":\"test-token\"" +
                "}";
        final var resultBody = "{\"Error message\":\"Web token expired\"}";
        mockMvc.perform(
                        post("/api/v1/playlist/new")
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(content().json(resultBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnApiCallProblemWithStatusTooManyRequestsAndProperMessageWhenAfterAllRepetitionsPlaylistCannotBeCreated() throws Exception {
        when(playlistService.buildPlaylist(any(PlaylistRequest.class))).thenThrow(new TooManyRequestsException());
        final var requestBody = "{" +
                "\"name\":\"test-name\"," +
                "\"sentence\":\"Input sentence\"," +
                "\"token\":\"test-token\"" +
                "}";
        final var resultBody = "{\"Error message\":\"Exceeded number of allowed calls to Spotify API. Please try again later.\"}";
        mockMvc.perform(
                        post("/api/v1/playlist/new")
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(content().json(resultBody))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void shouldReturnApiCallProblemWithStatusUnprocessableEntityAndProperMessageWhenServiceThrowsTracksNotFoundException() throws Exception {
        when(playlistService.buildPlaylist(any(PlaylistRequest.class))).thenThrow(new TracksNotFoundException("test message"));
        final var requestBody = "{" +
                "\"name\":\"test-name\"," +
                "\"sentence\":\"Input sentence\"," +
                "\"token\":\"test-token\"" +
                "}";
        final var resultBody = "{\"Error message\":\"Algorithm failed to find exact match for given sentence. " +
                "Please check if it contains any misspelled words and/or consider shorter request.\"}";
        mockMvc.perform(
                        post("/api/v1/playlist/new")
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andExpect(content().json(resultBody))
                .andExpect(status().isUnprocessableEntity());
    }
}