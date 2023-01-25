package walerowicz.pawel.SpotifyFun.playlist;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import walerowicz.pawel.SpotifyFun.authorization.User;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

@Service
class SpotifyService {
    Logger logger = LoggerFactory.getLogger(SpotifyService.class);
    private final HTTPRequestWrapper httpRequestWrapper;
    private final String userProfileURL;
    private final String createPlaylistURL;
    private final String addItemToPlaylistURL;
    private final String searchForTrackURL;

    @Autowired
    public SpotifyService(HTTPRequestWrapper httpRequestWrapper,
                          @Value("${spotify.user.profileURL}") final String userProfileURL,
                          @Value("${spotify.playlist.create}") final String createPlaylistURL,
                          @Value("${spotify.playlist.item.add}") final String addItemToPlaylistURL,
                          @Value("${spotify.search.track}") final String searchForTrackURL) {
        this.httpRequestWrapper = httpRequestWrapper;
        this.userProfileURL = userProfileURL;
        this.createPlaylistURL = createPlaylistURL;
        this.addItemToPlaylistURL = addItemToPlaylistURL;
        this.searchForTrackURL = searchForTrackURL;
    }

    String buildPlaylist(final String playlistName, final String inputSentence) throws URISyntaxException, JsonProcessingException {
        logger.info("Creating playlist '{}' from sentence '{}'", playlistName, inputSentence);
        final User user = importUser();
        final Playlist playlist = createNewPlaylist(playlistName, user);
        List<String> singleWords = splitSentence(inputSentence);
        List<List<Track>> titles = new LinkedList<>();
        try {
            for (int i = 0; i < singleWords.size(); i++) {
                String singleWord = singleWords.get(i);
                titles.add(searchForTracks(singleWord));
                logger.info("Found tracks for phrase '{}': {}", singleWord, titles.get(i));
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        addToPlaylist(playlist, titles);
        return playlist.externalUrls().url();
    }

    private User importUser() throws URISyntaxException {
        URI request = new URI(userProfileURL);
        return httpRequestWrapper.sentGetRequest(request, User.class);
    }

    private Playlist createNewPlaylist(final String playlistName, User user) throws URISyntaxException {
        final URI request = new URI(createPlaylistURL.replace("<USER_ID>", user.id()));
        final String body = "{\"name\": \"" + playlistName + "\"}";
        return httpRequestWrapper.sentPostRequest(request, body, Playlist.class);
    }

    private void addToPlaylist(final Playlist playlist, final List<List<Track>> tracks)
            throws URISyntaxException, JsonProcessingException {
        final URI request = new URI(addItemToPlaylistURL.replace("<PLAYLIST_ID>", playlist.id()));
        Random random = new Random();
        List<String> finalTracks = new ArrayList<>();
        for (List<Track> matchingTracks : tracks) {
            if (matchingTracks.size() > 0) {
                finalTracks.add("spotify:track:" + matchingTracks.get(random.nextInt(matchingTracks.size())).id());
            }
        }
        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(finalTracks);
        httpRequestWrapper.sentPostRequest(request, body, String.class);
    }

    private List<String> splitSentence(final String inputSentence) {
        return Arrays.stream(inputSentence.split("[! .,-]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private List<Track> searchForTracks(final String inputPhrase) throws URISyntaxException {
        final URI request = new URI(searchForTrackURL.replace("<QUERY>", inputPhrase));
        FoundTracksResultPackage foundTracksResultPackage = httpRequestWrapper.sentGetRequest(request, SearchResult.class).foundTracksResultPackage();
        List<Track> matches = filterTracks(foundTracksResultPackage.getFoundTracks(), inputPhrase);

        int counter = 1;
        while (matches.isEmpty() && counter < 50) {
            logger.debug("Searching for {} ({} attempt)", inputPhrase, counter);
            foundTracksResultPackage = httpRequestWrapper.sentGetRequest(foundTracksResultPackage.getNextURL(), SearchResult.class).foundTracksResultPackage();
            matches = filterTracks(foundTracksResultPackage.getFoundTracks(), inputPhrase);
            counter++;
        }
        return matches;
    }

    private List<Track> filterTracks(final List<Track> foundTracks, final String inputPhrase) {
        return foundTracks.stream().filter(track -> track.name().equalsIgnoreCase(inputPhrase)).collect(Collectors.toList());
    }
}