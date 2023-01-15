package walerowicz.pawel.SpotifyFun;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MyController {
    private final TokenRetriever tokenRetriever;
    private final SpotifyService spotifyService;

    @Autowired
    public MyController(TokenRetriever tokenRetriever, SpotifyService spotifyService) {
        this.tokenRetriever = tokenRetriever;
        this.spotifyService = spotifyService;
    }

    @Autowired

    @GetMapping("/hehe")
    public String somethingIGuess() {
        return "hehe";
    }

    @GetMapping("/token")
    public SpotifyAccessToken token() {
        return tokenRetriever.retrieveToken();
    }

    @GetMapping("/search")
    public void searchForTracks() {
        spotifyService.getTracks("angle");
    }
}
