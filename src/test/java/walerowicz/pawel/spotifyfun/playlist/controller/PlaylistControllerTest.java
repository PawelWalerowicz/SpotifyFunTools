package walerowicz.pawel.spotifyfun.playlist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import walerowicz.pawel.spotifyfun.authorization.controller.AuthorizationController;
import walerowicz.pawel.spotifyfun.authorization.service.SpotifyAuthorizationService;
import walerowicz.pawel.spotifyfun.playlist.entity.Playlist;
import walerowicz.pawel.spotifyfun.playlist.entity.PlaylistRequest;
import walerowicz.pawel.spotifyfun.playlist.entity.PlaylistUrl;
import walerowicz.pawel.spotifyfun.playlist.service.PlaylistService;

import java.net.http.HttpHeaders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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


}