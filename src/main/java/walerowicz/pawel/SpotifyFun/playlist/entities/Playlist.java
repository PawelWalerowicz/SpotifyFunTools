package walerowicz.pawel.SpotifyFun.playlist.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Playlist(@JsonProperty("external_urls") ExternalUrls externalUrls,
                       String href,
                       String id
) {
}
