package walerowicz.pawel.SpotifyFun.authorization;

public class AuthorizationException extends RuntimeException {

    public AuthorizationException(final String message) {
        super(message);
    }

    public AuthorizationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}