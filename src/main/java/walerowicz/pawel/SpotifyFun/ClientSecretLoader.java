package walerowicz.pawel.SpotifyFun;

import org.springframework.stereotype.Service;
import walerowicz.pawel.SpotifyFun.authorization.AuthorizationException;
import walerowicz.pawel.SpotifyFun.authorization.SpotifyAuthorizationService;

import java.util.Objects;
import java.util.Scanner;

@Service
public class ClientSecretLoader {

    public String loadClientSecret(final String fileName) {
        try {
            final var resourceAsStream = ClientSecretLoader.class
                                                .getClassLoader()
                                                .getResourceAsStream(fileName);
            final var scanner = new Scanner(Objects.requireNonNull(resourceAsStream));
            return scanner.nextLine();
        } catch (final NullPointerException e) {
            throw new AuthorizationException("Exception occurred during retrieval of client secret " +
                    "for required authorization. Check resources for file " + fileName, e);
        }
    }
}