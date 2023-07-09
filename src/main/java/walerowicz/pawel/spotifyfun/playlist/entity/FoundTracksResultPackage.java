package walerowicz.pawel.spotifyfun.playlist.entity;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.net.URI;
import java.util.List;

public record FoundTracksResultPackage(
        @JsonAlias("href") URI currentURL,
        @JsonAlias("next") URI nextURL,
        @JsonAlias("items") List<Track> foundTracks
) {
}
