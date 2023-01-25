package walerowicz.pawel.SpotifyFun.authorization;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Objects;
import java.util.Scanner;

@Service
class ClientSecretLoader {
    private final String clientSecretFileName;

    ClientSecretLoader(@Value("${spotify.clientSecretFileName}") final String clientSecretFileName) {
        this.clientSecretFileName = clientSecretFileName;
    }

    String loadClientSecret() {
        try {
            final InputStream resourceAsStream = SpotifyAuthorizationService.class
                                                .getClassLoader()
                                                .getResourceAsStream(clientSecretFileName);
            final Scanner scanner = new Scanner(Objects.requireNonNull(resourceAsStream));
            return scanner.nextLine();
        } catch (NullPointerException e) {
            throw new AuthorizationException("Exception occurred during retrieval of client secret " +
                    "for Spotify authorization. Check resources for file " + clientSecretFileName, e);
        }
    }
}