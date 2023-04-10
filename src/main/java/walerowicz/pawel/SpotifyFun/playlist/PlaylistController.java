package walerowicz.pawel.SpotifyFun.playlist;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import walerowicz.pawel.SpotifyFun.playlist.entities.PlaylistRequest;
import walerowicz.pawel.SpotifyFun.playlist.entities.PlaylistUrl;
import walerowicz.pawel.SpotifyFun.spellcheck.SpellCheck;

import java.net.URISyntaxException;

@RestController
public class PlaylistController {
    private final PlaylistGenerator spotifyService;
    private final SpellCheck spellCheck;

    @Autowired
    public PlaylistController(final PlaylistGenerator spotifyService, final SpellCheck spellCheck) {
        this.spotifyService = spotifyService;
        this.spellCheck = spellCheck;
    }

    @PostMapping("/playlist/new")
    public PlaylistUrl createPlaylist(@RequestBody PlaylistRequest playlistRequest) {
        PlaylistUrl playlistURL = null;
        try{
            final var fixedSentence = spellCheck.correctSpelling(playlistRequest.sentence());
            playlistURL = spotifyService.buildPlaylist(playlistRequest.name(), fixedSentence);
        } catch (URISyntaxException | JsonProcessingException | CombinationNotFoundException e) {
            e.printStackTrace();
        }
        return playlistURL;
    }
}
