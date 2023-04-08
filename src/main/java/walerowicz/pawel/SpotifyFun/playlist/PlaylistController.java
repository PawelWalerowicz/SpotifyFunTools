package walerowicz.pawel.SpotifyFun.playlist;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import walerowicz.pawel.SpotifyFun.playlist.entities.PlaylistRequest;
import walerowicz.pawel.SpotifyFun.playlist.entities.PlaylistUrl;

import java.net.URISyntaxException;

@RestController
public class PlaylistController {
    private final PlaylistGenerator spotifyService;

    @Autowired
    public PlaylistController(PlaylistGenerator spotifyService) {
        this.spotifyService = spotifyService;
    }

    @PostMapping("/playlist/new")
    public PlaylistUrl createPlaylist(@RequestBody PlaylistRequest playlistRequest) {
        PlaylistUrl playlistURL = null;
        try {
            playlistURL = spotifyService.buildPlaylist(playlistRequest.name(), playlistRequest.sentence());
        } catch (URISyntaxException | JsonProcessingException | CombinationNotFoundException e) {
            e.printStackTrace();
        }
        return playlistURL;
    }
}
