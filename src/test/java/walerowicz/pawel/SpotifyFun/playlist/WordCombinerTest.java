package walerowicz.pawel.SpotifyFun.playlist;

import org.junit.jupiter.api.Test;
import walerowicz.pawel.SpotifyFun.playlist.entities.Combination;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordCombinerTest {
    private WordCombiner combiner;
    private final String CLEANUP_REGEX = "[! .,-]";

    @Test
    void combinationsShouldBeCompleteWhenJoinLimitIsHigherThanAmountOfSpaceSeparatedPhrasesInInputSentence() {
        combiner = new WordCombiner(10, CLEANUP_REGEX);
        final String inputSentence = "a b c d";
        //when joinLimit>= single words expected amount of combinations equals to 2^(amount of words -1)
        double expectedAmountOfCombinations = Math.pow(2, inputSentence.split(" ").length - 1);
        final List<Combination> combinations = combiner.buildCombinations(inputSentence);
        assertTrue(combinations.contains(new Combination("a", "b", "c", "d")));
        assertTrue(combinations.contains(new Combination("a b", "c", "d")));
        assertTrue(combinations.contains(new Combination("a b", "c d")));
        assertTrue(combinations.contains(new Combination("a b c", "d")));
        assertTrue(combinations.contains(new Combination("a b c d")));
        assertTrue(combinations.contains(new Combination("a", "b c", "d")));
        assertTrue(combinations.contains(new Combination("a", "b c d")));
        assertTrue(combinations.contains(new Combination("a", "b", "c d")));
        assertEquals(expectedAmountOfCombinations, combinations.size());
    }

    @Test
    void combinationsShouldNotContainSinglePhrasesConsistingOfMoreWordsThanJoinLimit() {
        final int jointLimit = 2;
        combiner = new WordCombiner(2, CLEANUP_REGEX);
        final String inputSentence = "a b c d";
        final List<Combination> combinations = combiner.buildCombinations(inputSentence);
        assertTrue(combinations.contains(new Combination("a", "b", "c", "d")));
        assertTrue(combinations.contains(new Combination("a b", "c", "d")));
        assertTrue(combinations.contains(new Combination("a b", "c d")));
        assertTrue(combinations.contains(new Combination("a", "b c", "d")));
        assertTrue(combinations.contains(new Combination("a", "b", "c d")));

        assertFalse(combinations.contains(new Combination("a", "b c d")));
        assertFalse(combinations.contains(new Combination("a b c", "d")));
        assertFalse(combinations.contains(new Combination("a b c d")));
        assertEquals(5, combinations.size());

        for(Combination comb: combinations) {
            comb.getPhraseList().forEach(phrase -> assertTrue(phrase.split(" ").length<=jointLimit));
        }
    }

    @Test
    void combinationsShouldNotSplitJoinedWordsInInputSentence() {
        combiner = new WordCombiner(10, CLEANUP_REGEX);
        final String inputSentence = "ab c d";
        double expectedAmountOfCombinations = Math.pow(2, inputSentence.split(" ").length - 1);
        final List<Combination> combinations = combiner.buildCombinations(inputSentence);
        assertTrue(combinations.contains(new Combination("ab", "c", "d")));
        assertTrue(combinations.contains(new Combination("ab", "c d")));
        assertTrue(combinations.contains(new Combination("ab c", "d")));
        assertTrue(combinations.contains(new Combination("ab c d")));
        assertEquals(expectedAmountOfCombinations, combinations.size());
    }

    @Test
    void shouldRemoveAllSpecialCharactersFromInputSentence() {
        combiner = new WordCombiner(10, CLEANUP_REGEX);
        final String inputSentence = "One! two,three,four?,,five-six'seven";
        //when joinLimit>= single words expected amount of combinations equals to 2^(amount of words -1)
        List<Combination> combinations = combiner.buildCombinations(inputSentence);
        System.out.println(combinations);
        assertTrue(combinations.contains(new Combination("One", "two", "three", "four?", "five", "six'seven")));
    }

    @Test
    void shouldDistinctGivenPhrases() {
        combiner = new WordCombiner(10, CLEANUP_REGEX);
        List<Combination> givenCombinations = new ArrayList<>();
        givenCombinations.add(new Combination("a", "b", "c", "d"));
        givenCombinations.add(new Combination("a", "b"));
        givenCombinations.add(new Combination("a", "d"));
        givenCombinations.add(new Combination("a", "b", "b", "e"));
        givenCombinations.add(new Combination("b", "e"));
        List<String> returnedQueries = combiner.distinctQueries(givenCombinations);
        assertTrue(returnedQueries.containsAll(List.of("a", "b", "c", "d", "e")));
        assertEquals(List.of("a", "b", "c", "d", "e").size(), returnedQueries.size());
    }

}