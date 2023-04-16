package walerowicz.pawel.SpotifyFun;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import walerowicz.pawel.SpotifyFun.authorization.SpotifyAuthorizationService;

//TODO: add exception handling
@Controller
@Slf4j
public class PageController {
    private final SpotifyAuthorizationService spotifyAuthorizationService;

    @Autowired
    public PageController(SpotifyAuthorizationService spotifyAuthorizationService) {
        this.spotifyAuthorizationService = spotifyAuthorizationService;
    }

    @GetMapping("/playlist")
    public String playlist(@RequestParam String code) {
        spotifyAuthorizationService.retrieveAccessToken(code);
        log.info("A user logged in.");
        return "PlaylistGeneratorPage";
    }
}
