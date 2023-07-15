package walerowicz.pawel.spotifyfun.playlist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import walerowicz.pawel.spotifyfun.playlist.entity.PlaylistRequest;
import walerowicz.pawel.spotifyfun.playlist.entity.PlaylistUrl;
import walerowicz.pawel.spotifyfun.playlist.service.PlaylistService;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
@Validated
public class PlaylistController {
    private final PlaylistService playlistService;

    @PostMapping("/playlist/new")
    @ResponseStatus(value = HttpStatus.CREATED)
    public PlaylistUrl createPlaylist(@RequestBody @Valid final PlaylistRequest playlistRequest) {
        return playlistService.buildPlaylist(playlistRequest);
    }
}
