package walerowicz.pawel.SpotifyFun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import walerowicz.pawel.SpotifyFun.authorization.SpotifyAuthorizationService;

//TODO: add exception handling
@Controller
public class PageController {
    private final Logger logger = LoggerFactory.getLogger(PageController.class);
    private final SpotifyAuthorizationService spotifyAuthorizationService;

    @Autowired
    public PageController(SpotifyAuthorizationService spotifyAuthorizationService) {
        this.spotifyAuthorizationService = spotifyAuthorizationService;
    }

    @GetMapping("/playlist")
    public String playlist(@RequestParam String code) {
        spotifyAuthorizationService.retrieveAccessToken(code);
        logger.info("A user logged in.");
        return "PlaylistGeneratorPage";
    }
}
