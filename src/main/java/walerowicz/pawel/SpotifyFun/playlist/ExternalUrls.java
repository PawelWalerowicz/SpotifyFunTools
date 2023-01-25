package walerowicz.pawel.SpotifyFun.playlist;

import com.fasterxml.jackson.annotation.JsonProperty;

record ExternalUrls(@JsonProperty("spotify") String url) {
}
