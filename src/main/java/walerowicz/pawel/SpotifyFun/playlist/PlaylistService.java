package walerowicz.pawel.SpotifyFun.playlist;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import walerowicz.pawel.SpotifyFun.authorization.User;
import walerowicz.pawel.SpotifyFun.playlist.entities.Combination;
import walerowicz.pawel.SpotifyFun.playlist.entities.Playlist;
import walerowicz.pawel.SpotifyFun.playlist.entities.Track;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

@Service
class PlaylistService {
    private static final Logger logger = LoggerFactory.getLogger(PlaylistService.class);
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
                           final WordCombiner wordCombiner,
                           @Value("${spotify.playlist.create}") final String createPlaylistURL,
                           @Value("${spotify.playlist.item.add}") final String addItemToPlaylistURL) {
        this.httpRequestWrapper = httpRequestWrapper;
        this.concurrentRequestProcessor = concurrentRequestProcessor;
        this.userService = userService;
        this.wordCombiner = wordCombiner;
        this.createPlaylistURL = createPlaylistURL;
        this.addItemToPlaylistURL = addItemToPlaylistURL;
    }

    String buildPlaylist(final String playlistName, final String inputSentence)
            throws URISyntaxException, JsonProcessingException, CombinationNotFoundException {
        logger.info("Creating playlist '{}' from sentence '{}'", playlistName, inputSentence);
        final List<Combination> combinations = wordCombiner.buildCombinations(inputSentence);
        final List<String> allQueries = wordCombiner.distinctQueries(combinations);
        final Map<String, List<Track>> matchingTracks = concurrentRequestProcessor.sendConcurrentRequests(allQueries);
        final List<Combination> workingCombinations = filterWorkingCombinations(combinations, matchingTracks);
        final Combination chosenCombination = chooseTightestCombination(workingCombinations);
        logger.info("Shortest found combination: {}", chosenCombination.getPhraseList());
        final Playlist playlist = createNewPlaylist(playlistName);
        addToPlaylist(playlist, mapCombination(chosenCombination, matchingTracks));
        return playlist.externalUrls().url();
    }

    private List<List<Track>> mapCombination(Combination chosenCombination, Map<String, List<Track>> matchingTracks) {
        return chosenCombination.getPhraseList().stream()
                .map(matchingTracks::get)
                .collect(Collectors.toList());
    }

    private Combination chooseTightestCombination(List<Combination> workingCombinations) throws CombinationNotFoundException {
        return workingCombinations.stream()
                .min(Comparator.comparingInt(Combination::getSize))
                .orElseThrow(() -> new CombinationNotFoundException("Couldn't find combination for given input sentence"));
    }

    private List<Combination> filterWorkingCombinations(final List<Combination> combinedWords,
                                                        final Map<String, List<Track>> matchingTracks) {
        List<Combination> workingCombinations = new ArrayList<>();
        combinedWords.stream()
                .filter(combination -> allMatchingTracksFound(combination, matchingTracks))
                .forEach(combination -> {
                    logger.info("Found working combination: {}", combination);
                    workingCombinations.add(combination);
                });
        logger.info("Found {} working combinations.", workingCombinations.size());
        return workingCombinations;
    }

    private boolean allMatchingTracksFound(final Combination combination, final Map<String, List<Track>> matchingTracks) {
        return combination.getPhraseList().stream()
                .map(phrase -> matchingTracks.get(phrase).size() != 0)
                .distinct()
                .noneMatch(bool -> bool.equals(false));
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
        final Random random = new Random();
        final List<String> finalTracks = new ArrayList<>();
        for (List<Track> matchingTracks : tracks) {
            if (matchingTracks.size() > 0) {
                finalTracks.add("spotify:track:" + matchingTracks.get(random.nextInt(matchingTracks.size())).id());
            }
        }
        final ObjectMapper objectMapper = new ObjectMapper();
        final String body = objectMapper.writeValueAsString(finalTracks);
        httpRequestWrapper.sentPostRequest(request, body, String.class);
    }
}