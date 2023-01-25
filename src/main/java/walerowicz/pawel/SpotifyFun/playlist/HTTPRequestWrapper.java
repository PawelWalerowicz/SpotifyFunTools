package walerowicz.pawel.SpotifyFun.playlist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import walerowicz.pawel.SpotifyFun.authorization.SpotifyAuthorizationService;

import java.net.URI;

@Service
class HTTPRequestWrapper {
    private final SpotifyAuthorizationService spotifyAuthorizationService;

    @Autowired
    HTTPRequestWrapper(SpotifyAuthorizationService spotifyAuthorizationService) {
        this.spotifyAuthorizationService = spotifyAuthorizationService;
    }

    <T> T sentGetRequest(final URI request, final Class<T> outputClass) {
        return sentRequest(request, HttpMethod.GET, null, outputClass);
    }

    <T> T sentPostRequest(final URI request, final String body, final Class<T> outputClass) {
        return sentRequest(request, HttpMethod.POST, body, outputClass);
    }

    <T> T sentRequest(final URI request, final HttpMethod method, final String body, final Class<T> outputClass) {
        final RestTemplate restTemplate = new RestTemplate();
        final HttpEntity<String> objectHttpEntity = new HttpEntity<>(body, buildRequestHeader());
        final ResponseEntity<T> responseEntity = restTemplate.exchange(request, method, objectHttpEntity, outputClass);
        return responseEntity.getBody();
    }


    private HttpHeaders buildRequestHeader() {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.set("Authorization", "Bearer " + spotifyAuthorizationService.getAccessToken().getToken());
        return header;
    }
}
