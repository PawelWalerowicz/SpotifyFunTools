package walerowicz.pawel.SpotifyFun.playlist.combinations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import walerowicz.pawel.SpotifyFun.playlist.TracksNotFoundException;
import walerowicz.pawel.SpotifyFun.playlist.concurrent.search.ConcurrentRequestProcessor;
import walerowicz.pawel.SpotifyFun.playlist.entities.Combination;
import walerowicz.pawel.SpotifyFun.playlist.entities.Track;
import walerowicz.pawel.SpotifyFun.playlist.entities.TracksWithPhrase;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CombinationMatcherTest {
    private CombinationMatcher combinationMatcher;
    @Mock
    private ConcurrentRequestProcessor requestProcessor;
    @Mock
    private WordCombiner wordCombiner;

    final String testToken = "test-token";
    final String inputSentence = "WordA WordB WordC";
    final List<Combination> combinations = List.of(
            new Combination("WordA", "WordB", "WordC"),
            new Combination("WordA WordB", "WordC"),
            new Combination("WordA", "WordB WordC"),
            new Combination("WordA WordB WordC")
    );
    final List<String> queries = List.of("WordA", "WordB", "WordC", "WordA WordB", "WordB WordC","WordA WordB WordC");

    @BeforeEach
    void reset() {
        this.combinationMatcher = new CombinationMatcher(requestProcessor, wordCombiner);
    }

    private Set<TracksWithPhrase> generateMatchingResponses(final Combination combination) {
        final var tracksWithPhraseHashSet = new HashSet<TracksWithPhrase>();
        for (String phrase : combination.getPhraseList()) {
            final var tracksWithPhrase = new TracksWithPhrase(
                    phrase,
                    List.of(
                            new Track("id1", "href1", phrase),
                            new Track("id2", "href2", phrase + " not exact"),
                            new Track("id3", "href3", phrase)
                    )
            );
            tracksWithPhraseHashSet.add(tracksWithPhrase);
        }
        return tracksWithPhraseHashSet;
    }

    @Test
    void shouldCallStopSendingRequestsAfterMatchingCombinationIsFound() {
        when(wordCombiner.buildCombinations(inputSentence)).thenReturn(combinations);
        when(wordCombiner.distinctQueries(combinations)).thenReturn(queries);
        final var responseTracks = generateMatchingResponses(combinations.get(0));
        when(requestProcessor.sendConcurrentRequests(queries, testToken))
                .thenReturn(responseTracks);
       combinationMatcher.findCombinationWithMatchingTracks(inputSentence, testToken);
       verify(requestProcessor, times(1)).stopSendingRequests();
    }

    @Test
    void shouldChoseCombinationsWithLeastTracksWhenMoreThanOneMatchingCombinationIsFound() {
        when(wordCombiner.buildCombinations(inputSentence)).thenReturn(combinations);
        when(wordCombiner.distinctQueries(combinations)).thenReturn(queries);
        final var responseTracks = generateMatchingResponses(combinations.get(0));
        responseTracks.addAll(generateMatchingResponses(combinations.get(1)));
        responseTracks.addAll(generateMatchingResponses(combinations.get(2)));
        responseTracks.addAll(generateMatchingResponses(combinations.get(3)));
        when(requestProcessor.sendConcurrentRequests(queries, testToken))
                .thenReturn(responseTracks);
        final var result = combinationMatcher.findCombinationWithMatchingTracks(inputSentence, testToken);
        assertEquals(1, result.size());
    }

}