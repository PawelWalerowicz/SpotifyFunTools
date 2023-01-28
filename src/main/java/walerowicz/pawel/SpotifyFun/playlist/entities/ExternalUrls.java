package walerowicz.pawel.SpotifyFun.playlist.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExternalUrls(@JsonProperty("spotify") String url) {
}
