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
import walerowicz.pawel.SpotifyFun.playlist.entities.TracksWithPhrase;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
class PlaylistService {
    private static final Logger logger = LoggerFactory.getLogger(PlaylistService.class);
    private final SpotifyAPIRequest spotifyAPIRequest;
    private final ConcurrentRequestProcessor concurrentRequestProcessor;
    private final UserService userService;
    private final WordCombiner wordCombiner;

    private final String createPlaylistURL;
    private final String addItemToPlaylistURL;

    @Autowired
    public PlaylistService(final SpotifyAPIRequest spotifyAPIRequest,
                           final ConcurrentRequestProcessor concurrentRequestProcessor,
                           final UserService userService,
                           final WordCombiner wordCombiner,
                           @Value("${spotify.playlist.create}") final String createPlaylistURL,
                           @Value("${spotify.playlist.item.add}") final String addItemToPlaylistURL) {
        this.spotifyAPIRequest = spotifyAPIRequest;
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
        final List<TracksWithPhrase> allMatchingTracks = concurrentRequestProcessor.sendConcurrentRequests(allQueries);
        final List<Combination> workingCombinations = filterWorkingCombinations(combinations, allMatchingTracks);
        final Combination chosenCombination = chooseTightestCombination(workingCombinations);
        final List<String> combinationPhrases = chosenCombination.getPhraseList();
        logger.info("Shortest found combination: {}", combinationPhrases);
        final Playlist playlist = createNewPlaylist(playlistName);
        addToPlaylist(playlist, mapCombination(combinationPhrases, allMatchingTracks));
        return playlist.externalUrls().url();
    }

    private List<TracksWithPhrase> mapCombination(List<String> combinationPhrases, List<TracksWithPhrase> allMatchingTracks) {
        return combinationPhrases.stream()
                .map(phrase -> getTrackForPhrase(phrase, allMatchingTracks))
                .collect(Collectors.toList());
    }

    private TracksWithPhrase getTrackForPhrase(final String phrase, final List<TracksWithPhrase> tracksWithPhrase) {
        return tracksWithPhrase.stream()
                .filter(tracks -> tracks.phrase().equalsIgnoreCase(phrase))
                .findFirst()
                .orElseThrow(() -> new CombinationNotFoundException("Couldn't find combination for given input sentence"));
    }

    private Combination chooseTightestCombination(List<Combination> workingCombinations) throws CombinationNotFoundException {
        return workingCombinations.stream()
                .min(Combination::compareTo)
                .orElseThrow(() -> new CombinationNotFoundException("Couldn't find combination for given input sentence"));
    }

    private List<Combination> filterWorkingCombinations(final List<Combination> combinedWords,
                                                        final List<TracksWithPhrase> matchingTracks) {
        final List<Combination> workingCombinations = new ArrayList<>();
        List<TracksWithPhrase> existingTracks = removeEmptyPhrases(matchingTracks);
        final List<String> tracksPhrases = getTracksPhrases(existingTracks);
        combinedWords.stream()
                .filter(combination -> allMatchingTracksFound(combination, tracksPhrases))
                .forEach(combination -> {
                    logger.info("Found working combination: {}", combination);
                    workingCombinations.add(combination);
                });
        logger.info("Found {} working combinations.", workingCombinations.size());
        return workingCombinations;
    }

    private List<TracksWithPhrase> removeEmptyPhrases(final List<TracksWithPhrase> matchingTracks) {
        return matchingTracks.stream()
                .filter(TracksWithPhrase::hasMatchingTracks)
                .collect(Collectors.toList());
    }

    private List<String> getTracksPhrases(List<TracksWithPhrase> matchingTracks) {
        return matchingTracks.stream()
                .filter(TracksWithPhrase::hasMatchingTracks)
                .map(TracksWithPhrase::phrase)
                .collect(Collectors.toList());
    }

    private boolean allMatchingTracksFound(final Combination combination, final List<String> tracksPhrases) {
        return tracksPhrases.containsAll(combination.getPhraseList());
    }

    private Playlist createNewPlaylist(final String playlistName) throws URISyntaxException {
        User user = userService.importUser();
        final URI request = new URI(createPlaylistURL.replace("<USER_ID>", user.id()));
        final String body = "{\"name\": \"" + playlistName + "\"}";
        return spotifyAPIRequest.post(request, body, Playlist.class);
    }

    private void addToPlaylist(final Playlist playlist, final List<TracksWithPhrase> tracks)
            throws URISyntaxException, JsonProcessingException {
        final URI request = new URI(addItemToPlaylistURL.replace("<PLAYLIST_ID>", playlist.id()));
        final Random random = new Random();
        final List<String> finalTracks = new ArrayList<>();
        for (TracksWithPhrase matchingTracks : tracks) {
            final List<Track> trackOptions = matchingTracks.matchingTracks();
            final int tracksAmount  = trackOptions.size();
            if (tracksAmount > 0) {
                finalTracks.add("spotify:track:" + trackOptions.get(random.nextInt(tracksAmount)).id());
            }
        }
        final ObjectMapper objectMapper = new ObjectMapper();
        final String body = objectMapper.writeValueAsString(finalTracks);
        spotifyAPIRequest.post(request, body, String.class);
    }
}