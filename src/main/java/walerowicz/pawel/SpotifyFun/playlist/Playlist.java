package walerowicz.pawel.SpotifyFun.playlist;

import com.fasterxml.jackson.annotation.JsonProperty;

record Playlist(@JsonProperty("external_urls") ExternalUrls externalUrls,
                       String href,
                       String id) {
}
