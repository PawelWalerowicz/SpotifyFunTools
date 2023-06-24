package walerowicz.pawel.SpotifyFun;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import walerowicz.pawel.SpotifyFun.authorization.AuthorizationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientSecretLoader {
    private final ResourceLoader resourceLoader;
    @Value("${spotify.secret.filename}")
    private final String spotifySecretFilename;

    public String loadSpotifyClientSecret() {
        return loadClientSecret(spotifySecretFilename);
    }

    private String loadClientSecret(final String fileName) {
        try {
            final var resource = resourceLoader.getResource("classpath:" + fileName);
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (final NullPointerException | IOException e) {
            log.error("Exception occurred during retrieval of client secret " +
                    "for required authorization. Check resources for file {}", fileName, e);
            throw new AuthorizationException("Exception occurred during retrieval of client secret", e);
        }
    }
}