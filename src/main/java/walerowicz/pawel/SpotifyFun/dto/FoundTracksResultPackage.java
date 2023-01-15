package walerowicz.pawel.SpotifyFun.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.List;

@Data
public class FoundTracksResultPackage {
    @JsonAlias("href")
    private String currentURL;
    @JsonAlias("next")
    private String nextURL;
    @JsonAlias("items")
    private List<Track> foundTracks;
}
