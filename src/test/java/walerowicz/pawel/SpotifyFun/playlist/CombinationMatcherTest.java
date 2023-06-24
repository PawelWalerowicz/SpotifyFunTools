//package walerowicz.pawel.SpotifyFun.playlist;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import walerowicz.pawel.SpotifyFun.playlist.combinations.CombinationMatcher;
//import walerowicz.pawel.SpotifyFun.playlist.combinations.WordCombiner;
//import walerowicz.pawel.SpotifyFun.playlist.concurrent.ConcurrentRequestProcessor;
//import walerowicz.pawel.SpotifyFun.playlist.entities.Combination;
//
//import java.util.List;
//
//@ExtendWith(MockitoExtension.class)
//class CombinationMatcherTest {
//    private CombinationMatcher combinationMatcher;
//    @Mock
//    private ConcurrentRequestProcessor requestProcessor;
//    @Mock
//    private WordCombiner wordCombiner;
//
//    final String inputSentence = "WordA WordB";
//    final List<Combination> combinations = List.of(
//            new Combination("WordA", "WordB"),
//            new Combination("WordA WordB")
//    );
//    final List<String> queries = List.of("WordA", "WordB", "WordA WordB");
//
//    @BeforeEach
//    void reset() {
//        this.combinationMatcher = new CombinationMatcher(requestProcessor, wordCombiner);
//    }
//
////    @Test
////    void shouldCallRequestProcessorUsingProperQueries() {
////        when(wordCombiner.buildCombinations(inputSentence)).thenReturn(combinations);
////        when(wordCombiner.distinctQueries(combinations)).thenReturn(queries);
////
////        combinationMatcher.findCombinationWithMatchingTracks(inputSentence);
////        verify(requestProcessor).sendConcurrentRequests(queries, any(Set.class));
////
////    }
//
//}