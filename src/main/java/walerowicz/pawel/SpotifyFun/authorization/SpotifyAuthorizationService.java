package walerowicz.pawel.SpotifyFun.authorization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class SpotifyAuthorizationService {
    private final ClientSecretLoader clientSecretLoader;
    private final String clientId;
    private final String grantType;
    private final String redirectURI;
    private final String tokenURL;
    private final String authorizeURL;
    private Optional<SpotifyAccessToken> accessToken;

    @Autowired
    SpotifyAuthorizationService(final ClientSecretLoader clientSecretLoader,
                                       @Value("${spotify.clientId}") final String clientId,
                                       @Value("${spotify.grantType}") final String grantType,
                                       @Value("${spotify.redirectURI}") final String redirectURI,
                                       @Value("${spotify.tokenURL}") final String tokenURL,
                                       @Value("${spotify.authorizeURL}") final String authorizeURL) {
        this.clientSecretLoader = clientSecretLoader;
        this.clientId = clientId;
        this.grantType = grantType;
        this.redirectURI = redirectURI;
        this.tokenURL = tokenURL;
        this.authorizeURL = authorizeURL;
    }

    public SpotifyAccessToken getAccessToken()  {
        return accessToken.orElseThrow(() -> new AuthorizationException(
                "Access token hasn't been created. Please login first."));
    }

    String getUserAuthorizationURL() throws UnsupportedEncodingException {
        final String arguments = "client_id=" + clientId
                + "&response_type=code"
                + "&redirect_uri=" + URLEncoder.encode(redirectURI, UTF_8.toString())
                + "&scope=playlist-modify-public";
        return authorizeURL + "?" + arguments;
    }

    void retrieveAccessToken(String authorizationCode) {
        final RestTemplate restTemplate = new RestTemplate();
        final ResponseEntity<SpotifyAccessToken> responseEntity = restTemplate.exchange(tokenURL,
                                                                                        HttpMethod.POST,
                                                                                        buildHttpEntity(authorizationCode),
                                                                                        SpotifyAccessToken.class);
        this.accessToken = Optional.ofNullable(responseEntity.getBody());    //TODO: include refreshing SOMEWHERE
    }

    private HttpEntity<MultiValueMap<String, String>> buildHttpEntity(final String authorizationCode) {
        return new HttpEntity<>(buildRequestBody(authorizationCode), buildRequestHeader());
    }

    private MultiValueMap<String, String> buildRequestBody(final String authorizationCode) {
        MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();
        bodyValues.put("grant_type", List.of(grantType));
        bodyValues.put("code", List.of(authorizationCode));
        bodyValues.put("redirect_uri", List.of(redirectURI));
        return bodyValues;
    }

    private HttpHeaders buildRequestHeader() {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        header.set("Authorization", buildEncodedAuthorizationValue());
        return header;
    }

    private String buildEncodedAuthorizationValue() {
        final String plainClientCredentials = clientId + ":" + clientSecretLoader.loadClientSecret();
        final String encodedClientCredentials = Base64.getEncoder().encodeToString(plainClientCredentials.getBytes());
        return "Basic " + encodedClientCredentials;
    }
}