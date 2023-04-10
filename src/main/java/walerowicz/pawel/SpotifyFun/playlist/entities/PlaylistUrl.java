package walerowicz.pawel.SpotifyFun.playlist.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PlaylistUrl(@JsonProperty("playlist") String url) {
}
