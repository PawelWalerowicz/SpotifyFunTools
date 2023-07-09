package walerowicz.pawel.spotifyfun.playlist.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PlaylistUrl(
        @JsonProperty("playlist") String url
) {
}
