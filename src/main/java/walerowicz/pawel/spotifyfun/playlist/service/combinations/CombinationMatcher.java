package walerowicz.pawel.spotifyfun.playlist.service.combinations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import walerowicz.pawel.spotifyfun.playlist.entity.Combination;
import walerowicz.pawel.spotifyfun.playlist.entity.TracksWithPhrase;
import walerowicz.pawel.spotifyfun.playlist.exception.TracksNotFoundException;
import walerowicz.pawel.spotifyfun.playlist.service.search.ConcurrentRequestProcessor;
import walerowicz.pawel.spotifyfun.playlist.service.search.Sleeper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CombinationMatcher {
    private static final int WAIT_PERIOD_MS = 1000; // 1s
    private static final int MAX_FETCHING_PERIODS = 30; // 30s

    private final ConcurrentRequestProcessor concurrentRequestProcessor;
    private final WordCombiner wordCombiner;

    public List<TracksWithPhrase> findCombinationWithMatchingTracks(final String inputSentence, final String token) {
        final var combinations = wordCombiner.buildCombinations(inputSentence);
        final var allQueries = wordCombiner.distinctQueries(combinations);
        final var allMatchingTracks = concurrentRequestProcessor.sendConcurrentRequests(allQueries, token);
        final var workingCombinations = checkUntilMatchIsFound(combinations, allMatchingTracks);
        return getFinalCombination(workingCombinations, allMatchingTracks);
    }

    private List<Combination> checkUntilMatchIsFound(final List<Combination> combinations,
                                                     final Set<TracksWithPhrase> allMatchingTracks) {
        List<Combination> workingCombinations;
        int attempt = 1;
        do {
            workingCombinations = filterWorkingCombinations(combinations, allMatchingTracks);
            log.info("Found {} working combinations.", workingCombinations.size());
            if (workingCombinations.isEmpty()) {
                Sleeper.sleep(WAIT_PERIOD_MS);
                attempt++;
            }
        } while (shouldContinue(workingCombinations, attempt));
        concurrentRequestProcessor.stopSendingRequests();
        log.info("Found {} matching combinations", workingCombinations.size());
        return workingCombinations;
    }

    private boolean shouldContinue(final List<Combination> workingCombinations,
                                   final int attempt) {
        return workingCombinations.isEmpty() && attempt <= MAX_FETCHING_PERIODS;
    }

    private List<Combination> filterWorkingCombinations(final List<Combination> combinedWords,
                                                        final Set<TracksWithPhrase> matchingTracks) {
        final var tracksPhrases = getNonEmptyTracksPhrases(matchingTracks);
        return combinedWords.stream()
                .filter(combination -> allMatchingTracksFound(combination, tracksPhrases))
                .toList();
    }

    private Set<String> getNonEmptyTracksPhrases(final Set<TracksWithPhrase> matchingTracks) {
        return matchingTracks.stream()
                .filter(TracksWithPhrase::hasMatchingTracks)
                .map(TracksWithPhrase::phrase)
                .collect(Collectors.toSet());
    }

    private List<TracksWithPhrase> getFinalCombination(final List<Combination> workingCombinations, final Set<TracksWithPhrase> allMatchingTracks) {
        final var chosenCombination = chooseShortestCombination(workingCombinations);
        final var combinationPhrases = chosenCombination.getPhraseList();
        log.info("Shortest found combination: {}", combinationPhrases);
        return mapCombination(combinationPhrases, allMatchingTracks);
    }

    private Combination chooseShortestCombination(List<Combination> workingCombinations) {
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
                .toList();
    }

    private TracksWithPhrase getTrackForPhrase(final String phrase, final Set<TracksWithPhrase> tracksWithPhrase) {
        return tracksWithPhrase.stream()
                .filter(tracks -> tracks.phrase().equalsIgnoreCase(phrase))
                .findFirst()
                .orElseThrow(() -> new TracksNotFoundException("Couldn't find combination for given input sentence"));
    }
}