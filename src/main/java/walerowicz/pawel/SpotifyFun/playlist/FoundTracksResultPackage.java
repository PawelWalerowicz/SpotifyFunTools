package walerowicz.pawel.SpotifyFun.playlist;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Data
class FoundTracksResultPackage {
    @JsonAlias("href")
    private URI currentURL;
    @JsonAlias("next")
    private URI nextURL;
    @JsonAlias("items")
    private List<Track> foundTracks;

    public void setCurrentURL(String currentURL) throws URISyntaxException {
        this.currentURL = new URI(currentURL);
    }

    public void setNextURL(String nextURL) throws URISyntaxException {
        this.nextURL = new URI(nextURL);
    }
}
