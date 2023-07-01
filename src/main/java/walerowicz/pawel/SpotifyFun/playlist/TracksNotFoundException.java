package walerowicz.pawel.SpotifyFun.playlist;

public class TracksNotFoundException extends RuntimeException {
    public TracksNotFoundException(String message) {
        super(message);
    }
}
