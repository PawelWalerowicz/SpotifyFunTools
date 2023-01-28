package walerowicz.pawel.SpotifyFun.playlist;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import walerowicz.pawel.SpotifyFun.authorization.User;
import walerowicz.pawel.SpotifyFun.playlist.entities.Playlist;
import walerowicz.pawel.SpotifyFun.playlist.entities.Track;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@Service
class PlaylistService {
    Logger logger = LoggerFactory.getLogger(PlaylistService.class);
    private final HTTPRequestWrapper httpRequestWrapper;
    private final ConcurrentRequestProcessor concurrentRequestProcessor;
    private final UserService userService;
    private final WordCombiner wordCombiner;

    private final String createPlaylistURL;
    private final String addItemToPlaylistURL;

    @Autowired
    public PlaylistService(final HTTPRequestWrapper httpRequestWrapper,
                           final ConcurrentRequestProcessor concurrentRequestProcessor,
                           final UserService userService,
                           WordCombiner wordCombiner,
                           @Value("${spotify.playlist.create}") final String createPlaylistURL,
                           @Value("${spotify.playlist.item.add}") final String addItemToPlaylistURL) {
        this.httpRequestWrapper = httpRequestWrapper;
        this.concurrentRequestProcessor = concurrentRequestProcessor;
        this.userService = userService;
        this.wordCombiner = wordCombiner;
        this.createPlaylistURL = createPlaylistURL;
        this.addItemToPlaylistURL = addItemToPlaylistURL;
    }

    String buildPlaylist(final String playlistName, final String inputSentence) throws URISyntaxException, JsonProcessingException, CombinationNotFoundException {
        logger.info("Creating playlist '{}' from sentence '{}'", playlistName, inputSentence);
        final List<List<String>> combinedWords = wordCombiner.prepareCombinations(inputSentence, 6);
        final List<String> allQueries = wordCombiner.distinctQueries(combinedWords);
        final Map<String, List<Track>> matchingTracks = concurrentRequestProcessor.sendConcurrentRequests(allQueries);
        final List<List<String>> workingCombinations = filterWorkingCombinations(combinedWords, matchingTracks);

        List<String> tightestCombination = chooseTightestCombination(workingCombinations);
        final Playlist playlist = createNewPlaylist(playlistName + " (tight)");
        addToPlaylist(playlist, mapCombination(tightestCombination, matchingTracks));

        List<String> randomCombination = chooseAnyCombination(workingCombinations);
        addToPlaylist(createNewPlaylist(playlistName + " (random)"), mapCombination(randomCombination, matchingTracks));

        List<String> easiestCombination = chooseEasiestCombination(workingCombinations);
        addToPlaylist(createNewPlaylist(playlistName + " (easy)"), mapCombination(easiestCombination, matchingTracks));
        return playlist.externalUrls().url();
    }

    private List<List<Track>> mapCombination(List<String> chosenCombination, Map<String, List<Track>> matchingTracks) {
        List<List<Track>> finalTrackList = new ArrayList<>();
        chosenCombination.forEach(title -> finalTrackList.add(matchingTracks.get(title)));
        return finalTrackList;
    }

    private List<String> chooseAnyCombination(List<List<String>> workingCombinations) throws CombinationNotFoundException {
        Random random = new Random();
        if(workingCombinations.size()>0) {
            return workingCombinations.get(random.nextInt(workingCombinations.size()-1));
        } else throw new CombinationNotFoundException("Couldn't find combination for given input sentence");
    }

    private List<String> chooseTightestCombination(List<List<String>> workingCombinations) throws CombinationNotFoundException {
        return workingCombinations.stream()
                .min(Comparator.comparingInt(List::size))
                .orElseThrow(() -> new CombinationNotFoundException("Couldn't find combination for given input sentence"));
    }

    private List<String> chooseEasiestCombination(List<List<String>> workingCombinations) throws CombinationNotFoundException {
        return workingCombinations.stream()
                .max(Comparator.comparingInt(List::size))
                .orElseThrow(() -> new CombinationNotFoundException("Couldn't find combination for given input sentence"));
    }

    private List<List<String>> filterWorkingCombinations(List<List<String>> combinedWords, Map<String, List<Track>> matchingTracks) {
        List<List<String>> workingCombinations = new ArrayList<>();
        for (List<String> singleCombination : combinedWords) {
            boolean containsAll = singleCombination.stream()
                    .map(phrase -> matchingTracks.get(phrase).size() != 0)
                    .distinct()
                    .noneMatch(bool -> bool.equals(false));
            if (containsAll) {
                logger.info("Found working combination: {}", singleCombination);
                workingCombinations.add(singleCombination);
            }
        }
        logger.info("Found {} working combinations.", workingCombinations.size());
        return workingCombinations;
    }

    private Playlist createNewPlaylist(final String playlistName) throws URISyntaxException {
        User user = userService.importUser();
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
}