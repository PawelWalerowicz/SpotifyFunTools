package walerowicz.pawel.SpotifyFun.authorization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
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
@Slf4j
public class SpotifyAuthorizationService {
    private static final String GRANT_TYPE = "authorization_code";
    private static final String TOKEN_URI = "https://accounts.spotify.com/api/token";
    private static final String AUTH_URI = "https://accounts.spotify.com/authorize";
    private final ClientSecretLoader clientSecretLoader;
    private final String clientId;
    private final String redirectURI;
    private final String spotifySecretFilename;
    private final WebClient webClient;

    public SpotifyAuthorizationService(final ClientSecretLoader clientSecretLoader,
                                       @Value("${spotify.clientId}") final String clientId,
                                       @Value("${spotify.uri.redirect}") final String redirectURI,
                                       @Value("${spotify.secret.filename}") final String spotifySecretFilename) {
        this.clientSecretLoader = clientSecretLoader;
        this.clientId = clientId;
        this.redirectURI = redirectURI;
        this.spotifySecretFilename = spotifySecretFilename;
        this.webClient = initializeWebClient();
    }

    public String getAuthorizationCodeURL() {
        try {
            return AUTH_URI + "?" + tryBuildParams();
        } catch (UnsupportedEncodingException e) {
            log.error("Exception occurred during build request params for AuthCode request", e);
            throw new AuthorizationException("Exception occurred during request building process. Please try again later");
        }
    }

    public SpotifyAccessToken fetchAccessToken(final String authorizationCode) {
        return webClient
                .post()
                .bodyValue(new TokenRequest(GRANT_TYPE, authorizationCode, redirectURI).toMultiValueMap())
                .retrieve()
                .bodyToMono(SpotifyAccessToken.class)
                .block();
    }

    private String tryBuildParams() throws UnsupportedEncodingException {
        return "client_id=" + clientId
                + "&response_type=code"
                + "&redirect_uri=" + URLEncoder.encode(redirectURI, UTF_8.toString())
                + "&scope=playlist-modify-public";
    }

    private WebClient initializeWebClient() {
        return WebClient.builder()
                .baseUrl(TOKEN_URI)
                .defaultHeaders(header -> {
                    header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    header.set("Authorization", buildEncodedAuthorizationValue());
                })
                .build();
    }

    private String buildEncodedAuthorizationValue() {
        final var plainClientCredentials = clientId + ":" + clientSecretLoader.loadClientSecret(spotifySecretFilename);
        final var encodedClientCredentials = Base64.getEncoder().encodeToString(plainClientCredentials.getBytes());
        return "Basic " + encodedClientCredentials;
    }
}