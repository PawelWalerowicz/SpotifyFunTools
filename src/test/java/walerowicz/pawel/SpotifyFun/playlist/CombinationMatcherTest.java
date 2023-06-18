package walerowicz.pawel.SpotifyFun.playlist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import walerowicz.pawel.SpotifyFun.playlist.entities.Combination;
import walerowicz.pawel.SpotifyFun.playlist.entities.TracksWithPhrase;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CombinationMatcherTest {
    private CombinationMatcher combinationMatcher;
    @Mock
    private ConcurrentRequestProcessor requestProcessor;
    @Mock
    private WordCombiner wordCombiner;

    final String inputSentence = "WordA WordB";
    final List<Combination> combinations = List.of(
            new Combination("WordA", "WordB"),
            new Combination("WordA WordB")
    );
    final List<String> queries = List.of("WordA", "WordB", "WordA WordB");

    @BeforeEach
    void reset() {
        this.combinationMatcher = new CombinationMatcher(requestProcessor, wordCombiner);
    }

//    @Test
//    void shouldCallRequestProcessorUsingProperQueries() {
//        when(wordCombiner.buildCombinations(inputSentence)).thenReturn(combinations);
//        when(wordCombiner.distinctQueries(combinations)).thenReturn(queries);
//
//        combinationMatcher.findCombinationWithMatchingTracks(inputSentence);
//        verify(requestProcessor).sendConcurrentRequests(queries, any(Set.class));
//
//    }

}