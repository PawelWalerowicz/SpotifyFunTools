package walerowicz.pawel.spotifyfun.test.utils.response;

import okhttp3.mockwebserver.MockResponse;
import walerowicz.pawel.spotifyfun.test.utils.ResourceLoader;

public class PlaylistResponseBuilder {
    private String bodyJson = "{}";
    private String contentType = "application/json";
    private int responseCode = 201;

    private  PlaylistResponseBuilder() {
    }

    public static PlaylistResponseBuilder newBuilder() {
        return new PlaylistResponseBuilder();
    }

    public PlaylistResponseBuilder withProperPlaylist() {
        this.bodyJson = ResourceLoader.getResource("playlist_response.json");
        return this;
    }

    public PlaylistResponseBuilder withContentType(final String contentType) {
        this.bodyJson = contentType;
        return this;
    }

    public PlaylistResponseBuilder withResponseCode(final int responseCode) {
        this.responseCode = responseCode;
        return this;
    }

    public MockResponse build() {
        return new MockResponse()
                .setBody(bodyJson)
                .addHeader("Content-Type", contentType)
                .setResponseCode(responseCode);
    }
}
