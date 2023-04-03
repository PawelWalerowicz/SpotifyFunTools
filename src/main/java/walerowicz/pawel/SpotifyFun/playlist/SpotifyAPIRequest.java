package walerowicz.pawel.SpotifyFun.playlist;

import dev.failsafe.Failsafe;
import dev.failsafe.FailsafeException;
import dev.failsafe.RetryPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import walerowicz.pawel.SpotifyFun.authorization.SpotifyAuthorizationService;

import java.net.URI;

@Service
class SpotifyAPIRequest {
    private final SpotifyAuthorizationService spotifyAuthorizationService;
    private final RetryPolicy<Object> retryPolicy;

    @Autowired
    SpotifyAPIRequest(final SpotifyAuthorizationService spotifyAuthorizationService,
                      final RetryPolicyConfiguration retryPolicyConfiguration) {
        this.spotifyAuthorizationService = spotifyAuthorizationService;
        this.retryPolicy = retryPolicyConfiguration.configureRetryPolicy(HttpClientErrorException.class);
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
        try {
            return Failsafe
                    .with(retryPolicy)
                    .get(() -> restTemplate.exchange(request, method, objectHttpEntity, outputClass).getBody());
        } catch (
                FailsafeException ignore) {
        }
        return null;
    }

    private HttpHeaders buildRequestHeader() {
        final HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.set("Authorization", "Bearer " + spotifyAuthorizationService.getAccessToken().getToken());
        return header;
    }
}