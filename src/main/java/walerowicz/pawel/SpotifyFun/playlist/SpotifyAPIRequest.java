package walerowicz.pawel.SpotifyFun.playlist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import walerowicz.pawel.SpotifyFun.authorization.SpotifyAuthorizationService;

import java.net.URI;

@Service
class SpotifyAPIRequest {
    private final SpotifyAuthorizationService spotifyAuthorizationService;
    @Autowired
    SpotifyAPIRequest(final SpotifyAuthorizationService spotifyAuthorizationService) {
        this.spotifyAuthorizationService = spotifyAuthorizationService;
    }

    <T> T get(final URI request, final Class<T> outputClass) {
        return send(request, HttpMethod.GET, null, outputClass);
    }

    <T> T post(final URI request, final String body, final Class<T> outputClass) {
        return send(request, HttpMethod.POST, body, outputClass);
    }

    private <T> T send(final URI request, final HttpMethod method, final String body, final Class<T> outputClass) {
        final RestTemplate restTemplate = new RestTemplate();
        final HttpEntity<String> objectHttpEntity = new HttpEntity<>(body, buildRequestHeader());
        return restTemplate.exchange(request, method, objectHttpEntity, outputClass).getBody();
    }

    private HttpHeaders buildRequestHeader() {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.set("Authorization", "Bearer " + spotifyAuthorizationService.getAccessToken().getToken());
        return header;
    }
}
