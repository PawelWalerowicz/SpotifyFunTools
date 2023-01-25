package walerowicz.pawel.SpotifyFun.playlist;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URISyntaxException;

@Controller
public class PlaylistController {
    private final SpotifyService spotifyService;

    @Autowired
    public PlaylistController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @PostMapping("/playlist")
    public RedirectView createPlaylist(@RequestBody PlaylistRequest playlistRequest) {
        RedirectView redirectView = new RedirectView();
        String playlistURL = null;
        try {
            playlistURL = spotifyService.buildPlaylist(playlistRequest.name(), playlistRequest.sentence());
        } catch (URISyntaxException | JsonProcessingException e) {
            e.printStackTrace();
        }
        redirectView.setUrl(playlistURL);
        return redirectView;
    }
}
