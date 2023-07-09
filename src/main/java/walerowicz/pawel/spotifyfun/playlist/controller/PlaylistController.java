package walerowicz.pawel.spotifyfun.playlist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    public ResponseEntity<PlaylistUrl> createPlaylist(@RequestBody @Valid final PlaylistRequest playlistRequest) {
        return new ResponseEntity<>(playlistService.buildPlaylist(playlistRequest), HttpStatus.CREATED);
    }
}
