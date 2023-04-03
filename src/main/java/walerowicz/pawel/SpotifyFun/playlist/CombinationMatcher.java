package walerowicz.pawel.SpotifyFun.playlist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import walerowicz.pawel.SpotifyFun.playlist.entities.Combination;
import walerowicz.pawel.SpotifyFun.playlist.entities.TracksWithPhrase;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

@Service
public class CombinationMatcher {
    private final Logger logger = LoggerFactory.getLogger(CombinationMatcher.class);
    private final ConcurrentRequestProcessor concurrentRequestProcessor;
    private final WordCombiner wordCombiner;

    @Autowired
    public CombinationMatcher(final ConcurrentRequestProcessor concurrentRequestProcessor, final WordCombiner wordCombiner) {
        this.concurrentRequestProcessor = concurrentRequestProcessor;
        this.wordCombiner = wordCombiner;
    }

    List<TracksWithPhrase> findCombinationWithMatchingTracks(final String inputSentence) {
        final var combinations = wordCombiner.buildCombinations(inputSentence);
        final var allQueries = wordCombiner.distinctQueries(combinations);
        final var allMatchingTracks = new CopyOnWriteArraySet<TracksWithPhrase>();
        concurrentRequestProcessor.sendConcurrentRequests(allQueries, allMatchingTracks);
        List<Combination> workingCombinations;
        do {
            workingCombinations = filterWorkingCombinations(combinations, allMatchingTracks);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (workingCombinations.isEmpty());
        concurrentRequestProcessor.stopSendingRequests();
        final var chosenCombination = chooseTightestCombination(workingCombinations);
        final var combinationPhrases = chosenCombination.getPhraseList();
        logger.info("Shortest found combination: {}", combinationPhrases);
        return mapCombination(combinationPhrases, allMatchingTracks);
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
                .orElseThrow(() -> new CombinationNotFoundException("Couldn't find combination for given input sentence"));
    }

    private Combination chooseTightestCombination(List<Combination> workingCombinations) throws CombinationNotFoundException {
        return workingCombinations.stream()
                .min(Combination::compareTo)
                .orElseThrow(() -> new CombinationNotFoundException("Couldn't find combination for given input sentence"));
    }

    private List<Combination> filterWorkingCombinations(final List<Combination> combinedWords,
                                                        final Set<TracksWithPhrase> matchingTracks) {
        logger.debug("Filtering working combinations out of {}", matchingTracks);
        final var workingCombinations = new ArrayList<Combination>();
        final var tracksPhrases = getNonEmptyTracksPhrases(matchingTracks);
        combinedWords.stream()
                .filter(combination -> allMatchingTracksFound(combination, tracksPhrases))
                .forEach(combination -> {
                    logger.debug("Found working combination: {}", combination);
                    workingCombinations.add(combination);
                });
        logger.info("Found {} working combinations.", workingCombinations.size());
        return workingCombinations;
}


    private Set<String> getNonEmptyTracksPhrases(Set<TracksWithPhrase> matchingTracks) {
        return matchingTracks.stream()
                .filter(TracksWithPhrase::hasMatchingTracks)
                .map(TracksWithPhrase::phrase)
                .collect(Collectors.toSet());
    }

    private boolean allMatchingTracksFound(final Combination combination, final Set<String> tracksPhrases) {
        return tracksPhrases.containsAll(combination.getPhraseList());
    }
}