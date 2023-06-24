package walerowicz.pawel.SpotifyFun.playlist;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import walerowicz.pawel.SpotifyFun.playlist.entities.PlaylistRequest;
import walerowicz.pawel.SpotifyFun.playlist.entities.PlaylistUrl;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1")
public class PlaylistController {
    private final PlaylistGenerator spotifyService;

    @PostMapping("/playlist/new")
    public PlaylistUrl createPlaylist(@RequestBody final PlaylistRequest playlistRequest) {
        return spotifyService.buildPlaylist(playlistRequest);
    }
}
