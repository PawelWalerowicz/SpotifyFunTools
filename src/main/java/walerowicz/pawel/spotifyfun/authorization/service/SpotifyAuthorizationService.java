package walerowicz.pawel.spotifyfun.authorization.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import walerowicz.pawel.spotifyfun.authorization.entity.SpotifyAccessToken;
import walerowicz.pawel.spotifyfun.authorization.entity.TokenRequest;
import walerowicz.pawel.spotifyfun.authorization.exception.AuthorizationException;
import walerowicz.pawel.spotifyfun.configuration.ClientSecretLoader;
import walerowicz.pawel.spotifyfun.playlist.exception.TooManyRequestsException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@Slf4j
public class SpotifyAuthorizationService {
    private static final String GRANT_TYPE = "authorization_code";
    private static final String AUTH_URI = "https://accounts.spotify.com/authorize";

    private final String clientId;
    private final String redirectURI;
    private final WebClient webClient;
    private final String encodedCredentials;
    private final String authorizationCodeURL;

    public SpotifyAuthorizationService(final ClientSecretLoader clientSecretLoader,
                                       @Value("${spotify.clientId}") final String clientId,
                                       @Value("${spotify.uri.redirect}") final String redirectURI,
                                       @Qualifier("account") final WebClient webClient) {
        this.clientId = clientId;
        this.redirectURI = redirectURI;
        this.webClient = webClient;
        this.encodedCredentials = setEncodedAuthorizationCredentials(clientSecretLoader);
        this.authorizationCodeURL = setAuthorizationCodeURL();
    }

    public String getAuthorizationCodeURL() {
        return authorizationCodeURL;
    }

    public SpotifyAccessToken fetchAccessToken(final String authorizationCode) {
        return webClient
                .post()
                .headers(httpHeaders -> httpHeaders.setBasicAuth(encodedCredentials))
                .bodyValue(new TokenRequest(GRANT_TYPE, authorizationCode, redirectURI).toMultiValueMap())
                .exchangeToMono(response -> {
                    final var httpStatusCode = response.statusCode();
                    if (httpStatusCode.equals(HttpStatus.TOO_MANY_REQUESTS)) {
                        return Mono.error(new TooManyRequestsException());
                    } else if (httpStatusCode.equals(HttpStatus.UNAUTHORIZED)) {
                        return Mono.error(new AuthorizationException("Invalid authorization token"));
                    } else {
                        return response.bodyToMono(SpotifyAccessToken.class);
                    }
                })
                .block();
    }

    private String setEncodedAuthorizationCredentials(final ClientSecretLoader clientSecretLoader) {
        final String clientSecret = clientSecretLoader.loadSpotifyClientSecret();
        final var plainClientCredentials = clientId + ":" + clientSecret;
        return Base64.getEncoder().encodeToString(plainClientCredentials.getBytes());
    }

    private String setAuthorizationCodeURL() {
        return AUTH_URI + "?" + tryBuildParams();
    }

    private String tryBuildParams() {
        try {
            return buildParams();
        } catch (final UnsupportedEncodingException e) {
            log.error("Exception occurred during build request params for AuthCode request", e);
            throw new AuthorizationException("Exception occurred during request building process. Please try again later");
        }
    }

    private String buildParams() throws UnsupportedEncodingException {
        return "client_id=" + clientId
                + "&response_type=code"
                + "&redirect_uri=" + URLEncoder.encode(redirectURI, UTF_8.toString())
                + "&scope=playlist-modify-public";
    }
}