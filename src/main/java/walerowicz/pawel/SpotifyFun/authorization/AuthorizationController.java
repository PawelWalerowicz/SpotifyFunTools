package walerowicz.pawel.SpotifyFun.authorization;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import walerowicz.pawel.SpotifyFun.authorization.entites.SpotifyAccessToken;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthorizationController {
    private final SpotifyAuthorizationService spotifyAuthorizationService;

    @GetMapping("/login")
    public RedirectView login() {
        return new RedirectView(spotifyAuthorizationService.getAuthorizationCodeURL());
    }

    @GetMapping("/token")
    public SpotifyAccessToken fetchToken(@RequestParam String code) {
        return spotifyAuthorizationService.fetchAccessToken(code);
    }
}