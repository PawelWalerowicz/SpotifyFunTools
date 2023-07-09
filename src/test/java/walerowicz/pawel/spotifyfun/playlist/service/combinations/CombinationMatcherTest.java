package walerowicz.pawel.spotifyfun.playlist.service.combinations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import walerowicz.pawel.spotifyfun.playlist.entity.Combination;
import walerowicz.pawel.spotifyfun.playlist.service.search.ConcurrentRequestProcessor;
import walerowicz.pawel.spotifyfun.playlist.test.utils.response.TracksWithPhrasesTestBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CombinationMatcherTest {
    private CombinationMatcher combinationMatcher;
    @Mock
    private ConcurrentRequestProcessor requestProcessor;
    @Mock
    private WordCombiner wordCombiner;

    final String testToken = "test-token";

    @BeforeEach
    void reset() {
        this.combinationMatcher = new CombinationMatcher(requestProcessor, wordCombiner);
    }

    @Test
    void shouldCallStopSendingRequestsAfterMatchingCombinationIsFound() {
        final var inputSentence = "WordA WordB WordC";
        final var testCombinations = List.of(
                new Combination("WordA", "WordB", "WordC")
        );
        final List<String> testQueries = List.of("WordA", "WordB", "WordC");

        when(wordCombiner.buildCombinations(inputSentence)).thenReturn(testCombinations);
        when(wordCombiner.distinctQueries(testCombinations)).thenReturn(testQueries);

        final var responseTracks = TracksWithPhrasesTestBuilder
                .newBuilder()
                .withPhraseWithMatchingTracks("WordA", 2)
                .withPhraseWithMatchingTracks("WordB", 2)
                .withPhraseWithMatchingTracks("WordC", 2)
                .buildAsSet();

        when(requestProcessor.sendConcurrentRequests(testQueries, testToken)).thenReturn(responseTracks);

        combinationMatcher.findCombinationWithMatchingTracks(inputSentence, testToken);

        verify(requestProcessor, times(1)).stopSendingRequests();
    }

    @Test
    void shouldChoseCombinationsWithLeastTracksWhenMoreThanOneMatchingCombinationIsFound() {
        final var inputSentence = "WordA WordB WordC";
        final var testCombinations = List.of(
                new Combination("WordA", "WordB", "WordC"),
                new Combination("WordA WordB WordC")
        );
        final List<String> testQueries = List.of("WordA", "WordB", "WordC");

        when(wordCombiner.buildCombinations(inputSentence)).thenReturn(testCombinations);
        when(wordCombiner.distinctQueries(testCombinations)).thenReturn(testQueries);
        final var responseTracks = TracksWithPhrasesTestBuilder
                .newBuilder()
                .withPhraseWithMatchingTracks("WordA WordB WordC", 2)
                .withPhraseWithMatchingTracks("WordA", 10)
                .withPhraseWithMatchingTracks("WordB", 10)
                .withPhraseWithMatchingTracks("WordC", 10)
                .buildAsSet();
        when(requestProcessor.sendConcurrentRequests(testQueries, testToken)).thenReturn(responseTracks);
        final var result = combinationMatcher.findCombinationWithMatchingTracks(inputSentence, testToken);
        assertEquals(1, result.size());
    }

}