package walerowicz.pawel.SpotifyFun.authorization;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import walerowicz.pawel.SpotifyFun.ClientSecretLoader;
import walerowicz.pawel.SpotifyFun.authorization.entites.SpotifyAccessToken;
import walerowicz.pawel.SpotifyFun.authorization.entites.TokenRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotifyAuthorizationService {
    private static final String GRANT_TYPE = "authorization_code";
    private static final String AUTH_URI = "https://accounts.spotify.com/authorize";

    private final ClientSecretLoader clientSecretLoader;
    @Value("${spotify.clientId}")
    private final String clientId;
    @Value("${spotify.uri.redirect}")
    private final String redirectURI;
    @Qualifier("account")
    private final WebClient webClient;

    private String encodedCredentials;
    private String authorizationCodeURL;

    public String getAuthorizationCodeURL() {
        return authorizationCodeURL;
    }

    public SpotifyAccessToken fetchAccessToken(final String authorizationCode) {
        return webClient
                .post()
                .headers(httpHeaders -> httpHeaders.setBasicAuth(encodedCredentials))
                .bodyValue(new TokenRequest(GRANT_TYPE, authorizationCode, redirectURI).toMultiValueMap())
                .retrieve()
                .bodyToMono(SpotifyAccessToken.class)
                .block();
    }

    @PostConstruct
    private void setEncodedAuthorizationCredentials() {
        final var plainClientCredentials = clientId + ":" + clientSecretLoader.loadSpotifyClientSecret();
        this.encodedCredentials =  Base64.getEncoder().encodeToString(plainClientCredentials.getBytes());
    }

    @PostConstruct
    private void setAuthorizationCodeURL() {
        this.authorizationCodeURL = AUTH_URI + "?" + tryBuildParams();
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