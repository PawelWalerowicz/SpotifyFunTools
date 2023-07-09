package walerowicz.pawel.spotifyfun.playlist.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExternalUrls(
        @JsonProperty("spotify") String url
) {
}
