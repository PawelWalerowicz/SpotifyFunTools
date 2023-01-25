package walerowicz.pawel.SpotifyFun.authorization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.io.UnsupportedEncodingException;

//TODO: add exception handling
@RestController
public class AuthorizationController {
    private final SpotifyAuthorizationService spotifyAuthorizationService;

    @Autowired
    public AuthorizationController(SpotifyAuthorizationService spotifyAuthorizationService) {
        this.spotifyAuthorizationService = spotifyAuthorizationService;
    }

    @GetMapping("/hello")
    public String hello(@RequestParam String code) {
        spotifyAuthorizationService.retrieveAccessToken(code);
        return "Success!";  //Here we can redirect to subpage with simple UI with entry prompt for playlist name and input sentence
    }

    @GetMapping("/login")
    public RedirectView login() {
        RedirectView redirectView = new RedirectView();
        try {
            redirectView.setUrl(spotifyAuthorizationService.getUserAuthorizationURL());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return redirectView;
    }
}