package walerowicz.pawel.SpotifyFun.playlist;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import walerowicz.pawel.SpotifyFun.playlist.entities.PlaylistRequest;
import walerowicz.pawel.SpotifyFun.playlist.entities.PlaylistUrl;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
@Validated
public class PlaylistController {
    private final PlaylistService spotifyService;

    @PostMapping("/playlist/new")
    public PlaylistUrl createPlaylist(@RequestBody @Valid final PlaylistRequest playlistRequest) {
        return spotifyService.buildPlaylist(playlistRequest);
    }
}
