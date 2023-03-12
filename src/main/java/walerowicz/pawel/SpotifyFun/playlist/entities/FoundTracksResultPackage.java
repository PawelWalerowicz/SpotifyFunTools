package walerowicz.pawel.SpotifyFun.playlist.entities;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Data
public class FoundTracksResultPackage {
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
        if(nextURL!=null) this.nextURL = new URI(nextURL);
    }

    public boolean hasNextURL() {
        return nextURL!=null;
    }
}
