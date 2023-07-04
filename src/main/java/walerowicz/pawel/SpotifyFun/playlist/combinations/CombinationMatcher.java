package walerowicz.pawel.SpotifyFun.playlist.combinations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import walerowicz.pawel.SpotifyFun.playlist.TracksNotFoundException;
import walerowicz.pawel.SpotifyFun.playlist.concurrent.search.ConcurrentRequestProcessor;
import walerowicz.pawel.SpotifyFun.playlist.entities.Combination;
import walerowicz.pawel.SpotifyFun.playlist.entities.TracksWithPhrase;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CombinationMatcher {
    private static final int WAIT_PERIOD_MS = 1000; // 1s

    private final ConcurrentRequestProcessor concurrentRequestProcessor;
    private final WordCombiner wordCombiner;

    public List<TracksWithPhrase> findCombinationWithMatchingTracks(final String inputSentence, final String token) {
        final var combinations = wordCombiner.buildCombinations(inputSentence);
        final var allQueries = wordCombiner.distinctQueries(combinations);
        final var allMatchingTracks = concurrentRequestProcessor.sendConcurrentRequests(allQueries, token);
        List<Combination> workingCombinations;
        do {
            workingCombinations = filterWorkingCombinations(combinations, allMatchingTracks);
            waitBetweenChecks();
        } while (workingCombinations.isEmpty());
        concurrentRequestProcessor.stopSendingRequests();
        log.info("Found {} matching combinations", workingCombinations.size());
        final var chosenCombination = chooseTightestCombination(workingCombinations);
        final var combinationPhrases = chosenCombination.getPhraseList();
        log.info("Shortest found combination: {}", combinationPhrases);
        return mapCombination(combinationPhrases, allMatchingTracks);
    }

    private List<Combination> filterWorkingCombinations(final List<Combination> combinedWords,
                                                        final Set<TracksWithPhrase> matchingTracks) {
        log.debug("Filtering working combinations out of {}", matchingTracks);
        final var workingCombinations = new ArrayList<Combination>();
        final var tracksPhrases = getNonEmptyTracksPhrases(matchingTracks);
        combinedWords.stream()
                .filter(combination -> allMatchingTracksFound(combination, tracksPhrases))
                .forEach(combination -> {
                    log.debug("Found working combination: {}", combination);
                    workingCombinations.add(combination);
                });
        log.info("Found {} working combinations.", workingCombinations.size());
        return workingCombinations;
    }

    private Set<String> getNonEmptyTracksPhrases(Set<TracksWithPhrase> matchingTracks) {
        return matchingTracks.stream()
                .filter(TracksWithPhrase::hasMatchingTracks)
                .map(TracksWithPhrase::phrase)
                .collect(Collectors.toSet());
    }

    private Combination chooseTightestCombination(List<Combination> workingCombinations) {
        return workingCombinations.stream()
                .min(Combination::compareTo)
                .orElseThrow(() -> new TracksNotFoundException("Couldn't find combination for given input sentence"));
    }

    private boolean allMatchingTracksFound(final Combination combination, final Set<String> tracksPhrases) {
        return tracksPhrases.containsAll(combination.getPhraseList());
    }

    private List<TracksWithPhrase> mapCombination(List<String> combinationPhrases, Set<TracksWithPhrase> allMatchingTracks) {
        return combinationPhrases.stream()
                .map(phrase -> getTrackForPhrase(phrase, allMatchingTracks))
                .collect(Collectors.toList());
    }

    private TracksWithPhrase getTrackForPhrase(final String phrase, final Set<TracksWithPhrase> tracksWithPhrase) {
        return tracksWithPhrase.stream()
                .filter(tracks -> tracks.phrase().equalsIgnoreCase(phrase))
                .findFirst()
                .orElseThrow(() -> new TracksNotFoundException("Couldn't find combination for given input sentence"));
    }

    private void waitBetweenChecks() {
        try {
            Thread.sleep(WAIT_PERIOD_MS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}