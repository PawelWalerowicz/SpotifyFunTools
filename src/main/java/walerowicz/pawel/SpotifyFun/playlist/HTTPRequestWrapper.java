package walerowicz.pawel.SpotifyFun.playlist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import walerowicz.pawel.SpotifyFun.authorization.SpotifyAuthorizationService;

import java.net.URI;

@Service
class HTTPRequestWrapper {
    private final Logger logger = LoggerFactory.getLogger(HTTPRequestWrapper.class);
    private final SpotifyAuthorizationService spotifyAuthorizationService;

    @Autowired
    HTTPRequestWrapper(final SpotifyAuthorizationService spotifyAuthorizationService) {
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
        ResponseEntity<T> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(request, method, objectHttpEntity, outputClass);
        } catch (HttpClientErrorException e) {
            logger.warn("Too many requests occurred. Taking a 30 second break");
            try {
                Thread.sleep(21_000);
                logger.warn("(just 10 more seconds))");
                Thread.sleep(10_000);
                responseEntity = restTemplate.exchange(request, method, objectHttpEntity, outputClass);
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
        }
        return responseEntity.getBody();
    }

    private HttpHeaders buildRequestHeader() {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.set("Authorization", "Bearer " + spotifyAuthorizationService.getAccessToken().getToken());
        return header;
    }
}
