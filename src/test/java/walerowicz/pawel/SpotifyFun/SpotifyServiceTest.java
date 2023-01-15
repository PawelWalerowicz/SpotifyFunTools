package walerowicz.pawel.SpotifyFun;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import walerowicz.pawel.SpotifyFun.dto.SearchResult;

@ContextConfiguration(classes = SpotifyService.class)
class SpotifyServiceTest {

    @Test
    void sth() {
        TokenRetriever tokenRetriever = new TokenRetriever("6d1aba85011246b79d00fda8c9902ee3", "client_credentials", "https://accounts.spotify.com/api/token");
        SpotifyService spotifyService = new SpotifyService(tokenRetriever);
        SearchResult dicks = spotifyService.getTracks("Dicks");
        System.out.println(dicks);
    }

    @Test
    void builderTest() {
        TokenRetriever tokenRetriever = new TokenRetriever("6d1aba85011246b79d00fda8c9902ee3", "client_credentials", "https://accounts.spotify.com/api/token");
        SpotifyService spotifyService = new SpotifyService(tokenRetriever);
        spotifyService.buildPlaylist("long time ago");
    }

}