package walerowicz.pawel.spotifyfun.playlist.service.combinations;

import org.junit.jupiter.api.Test;
import walerowicz.pawel.spotifyfun.playlist.entity.Combination;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordCombinerTest {
    private final String CLEANUP_REGEX = "[! .,-]";

    @Test
    void combinationsShouldBeCompleteWhenJoinLimitIsHigherThanAmountOfSpaceSeparatedPhrasesInInputSentence() {
        final var combiner = new WordCombiner(10, CLEANUP_REGEX);
        final var inputSentence = "a b c d";
        //when joinLimit >= single words expected amount of combinations equals to 2^(amount of words -1)
        double expectedAmountOfCombinations = Math.pow(2, inputSentence.split(" ").length - 1);
        final var combinations = combiner.buildCombinations(inputSentence);
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
        final var jointLimit = 2;
        final var combiner = new WordCombiner(2, CLEANUP_REGEX);
        final var inputSentence = "a b c d";
        final var combinations = combiner.buildCombinations(inputSentence);
        assertTrue(combinations.contains(new Combination("a", "b", "c", "d")));
        assertTrue(combinations.contains(new Combination("a b", "c", "d")));
        assertTrue(combinations.contains(new Combination("a b", "c d")));
        assertTrue(combinations.contains(new Combination("a", "b c", "d")));
        assertTrue(combinations.contains(new Combination("a", "b", "c d")));

        assertFalse(combinations.contains(new Combination("a", "b c d")));
        assertFalse(combinations.contains(new Combination("a b c", "d")));
        assertFalse(combinations.contains(new Combination("a b c d")));
        assertEquals(5, combinations.size());

        for (Combination comb : combinations) {
            comb.getPhraseList().forEach(phrase -> assertTrue(phrase.split(" ").length <= jointLimit));
        }
    }

    @Test
    void combinationsShouldNotSplitJoinedWordsInInputSentence() {
        final var combiner = new WordCombiner(10, CLEANUP_REGEX);
        final var inputSentence = "ab c d";
        final var expectedAmountOfCombinations = Math.pow(2, inputSentence.split(" ").length - 1);
        final var combinations = combiner.buildCombinations(inputSentence);
        assertTrue(combinations.contains(new Combination("ab", "c", "d")));
        assertTrue(combinations.contains(new Combination("ab", "c d")));
        assertTrue(combinations.contains(new Combination("ab c", "d")));
        assertTrue(combinations.contains(new Combination("ab c d")));
        assertEquals(expectedAmountOfCombinations, combinations.size());
    }

    @Test
    void shouldRemoveAllSpecialCharactersFromInputSentence() {
        final var combiner = new WordCombiner(10, CLEANUP_REGEX);
        final var inputSentence = "One! two,three,four?,,five-six'seven";
        //when joinLimit>= single words expected amount of combinations equals to 2^(amount of words -1)
        final var combinations = combiner.buildCombinations(inputSentence);
        assertTrue(combinations.contains(new Combination("One", "two", "three", "four?", "five", "six'seven")));
    }

    @Test
    void shouldDistinctGivenPhrases() {
        final var combiner = new WordCombiner(10, CLEANUP_REGEX);
        final var givenCombinations = new ArrayList<Combination>();
        givenCombinations.add(new Combination("a", "b", "c", "d"));
        givenCombinations.add(new Combination("a", "b"));
        givenCombinations.add(new Combination("a", "d"));
        givenCombinations.add(new Combination("a", "b", "b", "e"));
        givenCombinations.add(new Combination("b", "e"));

        final var returnedQueries = combiner.distinctQueries(givenCombinations);

        assertTrue(returnedQueries.containsAll(List.of("a", "b", "c", "d", "e")));
        assertEquals(List.of("a", "b", "c", "d", "e").size(), returnedQueries.size());
    }
}