package walerowicz.pawel.SpotifyFun.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class SearchResult {
    @JsonAlias("tracks")
    private FoundTracksResultPackage foundTracksResultPackage;
}
