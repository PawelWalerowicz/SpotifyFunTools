package walerowicz.pawel.spotifyfun.playlist.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public record Playlist(
        @JsonProperty("external_urls") ExternalUrls externalUrls,
        String href,
        String id
) {
    public String getPlaylistUrl() {
        if (Objects.nonNull(externalUrls)) {
            return externalUrls.url();
        } else {
            throw new IllegalStateException("Playlist object does not contain URL");
        }
    }
}
