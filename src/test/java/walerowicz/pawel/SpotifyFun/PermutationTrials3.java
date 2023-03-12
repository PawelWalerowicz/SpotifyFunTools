package walerowicz.pawel.SpotifyFun;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PermutationTrials3 {
    @Test
    void sth() {
        String sentence = "one two three four five six seven eight nine ten";
//        String sentence = "one two three four";
        List<String> allWords = Arrays.asList(sentence.split("[! .,-]"));
        List<List<String>> combinations = combine(allWords, 10);
        combinations.forEach(System.out::println);
        System.out.println(combinations.size());
    }

    List<List<String>> combine(final List<String> allWords, final int joinLimit) {
        List<List<String>> allCombinations = new ArrayList<>();
        allCombinations.add(allWords);
        for (int joinedWords = 2; joinedWords <= Math.min(joinLimit, allWords.size()); joinedWords++) {
            allCombinations.addAll(combineSublist(joinedWords, allWords, joinLimit));
        }
        allCombinations = allCombinations.stream().distinct().collect(Collectors.toList());
        Collections.reverse(allCombinations);
        return allCombinations;
    }

    private List<List<String>> combineSublist(int joinedWords, List<String> allWords, int joinLimit) {
        List<List<String>> allSublists = new ArrayList<>();
        final int allWordsAmount = allWords.size();
        for (int startIndex = 0; startIndex < allWordsAmount + 1 - joinedWords; startIndex++) {
            int endIndex = startIndex + joinedWords;
            List<List<String>> leadingCombinations = combine(allWords.subList(0, startIndex), joinLimit);
            String joined = String.join(" ", allWords.subList(startIndex, endIndex));
            List<List<String>> tailingCombinations = combine(allWords.subList(endIndex, allWordsAmount), joinLimit);
            allSublists.addAll(flatMapWithLeadingAndTailing(leadingCombinations, joined, tailingCombinations));
        }
        return allSublists;
    }

    private List<List<String>> flatMapWithLeadingAndTailing(final List<List<String>> leadingCombinations,
                                                            final String joined,
                                                            final List<List<String>> tailingCombinations) {
        List<List<String>> solvedCombinations = new ArrayList<>();
        for (List<String> leadingList : leadingCombinations) {
            for (List<String> tailingList : tailingCombinations) {
                List<String> newCombination = new ArrayList<>(leadingList);
                newCombination.add(joined);
                newCombination.addAll(tailingList);
                solvedCombinations.add(newCombination);
            }
        }
        return solvedCombinations;
    }

//    private List<List<String>> flatMapWithLeadingAndTailing(List<List<String>> leadingCombinations, String joined, List<List<String>> tailingCombinations) {
//        List<List<String>> solvedCombinations = new ArrayList<>();
//        for (List<String> leadingList : leadingCombinations) {
//            for (List<String> tailingList : tailingCombinations) {
//                List<String> newCombination = new ArrayList<>();
//                newCombination.addAll(leadingList);
//                newCombination.add(joined);
//                newCombination.addAll(tailingList);
//                solvedCombinations.add(newCombination);
//            }
//        }
//        return solvedCombinations;
//    }

}
