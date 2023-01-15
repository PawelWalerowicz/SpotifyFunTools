package walerowicz.pawel.SpotifyFun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Objects;
import java.util.Scanner;

@Service
class TokenRetriever {
    private final static Logger logger = LoggerFactory.getLogger(TokenRetriever.class);
    private final static String CLIENT_SECRET_FILE_NAME = "client-secret.txt";

    private final String clientId;
    private final String grandType;
    private final String tokenURL;
    private SpotifyAccessToken spotifyAccessToken;

    public TokenRetriever(@Value("${spotify.clientId}") final String clientId,
                          @Value("${spotify.grandType}") final String grandType,
                          @Value("${spotify.tokenURL}") final String tokenURL) {
        this.clientId = clientId;
        this.grandType = grandType;
        this.tokenURL = tokenURL;
    }

    SpotifyAccessToken retrieveToken() {
        return spotifyAccessToken != null && spotifyAccessToken.isNotExpired() ? spotifyAccessToken : fetchNewToken();
    }

    private SpotifyAccessToken fetchNewToken() {
        final RestTemplate restTemplate = new RestTemplate();
        final ResponseEntity<SpotifyAccessToken> responseEntity = restTemplate.exchange(tokenURL, HttpMethod.POST, buildHttpEntity(), SpotifyAccessToken.class);
        SpotifyAccessToken accessToken = responseEntity.getBody();
        if (accessToken != null) {
            logger.debug("Successfully retrieved authorization token from Spotify");
            accessToken.setCreationTimestamp(LocalDateTime.now());
            this.spotifyAccessToken = accessToken;
            return spotifyAccessToken;
        } else
            throw new AuthorizationException("Exception occurred during retrieval of authorization token from Spotify." +
                    "Fetched entity: " + responseEntity);
    }

    private HttpEntity<String> buildHttpEntity() {
        return new HttpEntity<>(buildBodyWithGrandType(), buildRequestHeader());
    }

    private String buildBodyWithGrandType() {
        return "grant_type=" + grandType;
    }

    private HttpHeaders buildRequestHeader() {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        header.set("Authorization", buildEncodedAuthorizationValue());
        return header;
    }

    private String buildEncodedAuthorizationValue() {
        final String plainClientCredentials = clientId + ":" + loadClientSecret();
        final String encodedClientCredentials = Base64.getEncoder().encodeToString(plainClientCredentials.getBytes());
        return "Basic " + encodedClientCredentials;
    }

    private String loadClientSecret() {
        try {
            InputStream resourceAsStream = TokenRetriever.class.getClassLoader().getResourceAsStream(CLIENT_SECRET_FILE_NAME);
            final Scanner scanner = new Scanner(Objects.requireNonNull(resourceAsStream));
            return scanner.nextLine();
        } catch (NullPointerException e) {
            throw new AuthorizationException("Exception occurred during retrieval of client secret " +
                    "for Spotify authorization. Check resources for file " + CLIENT_SECRET_FILE_NAME, e);
        }
    }
}