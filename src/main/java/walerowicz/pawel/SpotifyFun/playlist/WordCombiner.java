package walerowicz.pawel.SpotifyFun.playlist;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
class WordCombiner {

    List<List<String>> prepareCombinations(final String inputSentence, final int joinLimit) {
        List<String> singleWords = splitSentence(inputSentence);
        return combine(singleWords, joinLimit);
    }

    List<String> distinctQueries(final List<List<String>> combinedWords) {
        return combinedWords.stream()
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> splitSentence(final String inputSentence) {
        return Arrays.stream(inputSentence.split("[! .,-]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private List<List<String>> combine(final List<String> allWords, final int joinLimit) {
        List<List<String>> allCombinations = new ArrayList<>();
        allCombinations.add(allWords);
        for (int joinedWords = 2; joinedWords <= Math.min(joinLimit, allWords.size()); joinedWords++) {
            allCombinations.addAll(combineSublist(joinedWords, allWords, joinLimit));
        }
        allCombinations = allCombinations.stream().distinct().collect(Collectors.toList());
        Collections.reverse(allCombinations);
        return allCombinations;
    }

    private List<List<String>> combineSublist(final int joinedWords, final List<String> allWords, final int joinLimit) {
        final List<List<String>> allSubLists = new ArrayList<>();
        final int allWordsAmount = allWords.size();
        for (int startIndex = 0; startIndex < allWordsAmount + 1 - joinedWords; startIndex++) {
            int endIndex = startIndex + joinedWords;
            List<List<String>> leadingCombinations = combine(allWords.subList(0, startIndex), joinLimit);
            String joined = String.join(" ", allWords.subList(startIndex, endIndex));
            List<List<String>> tailingCombinations = combine(allWords.subList(endIndex, allWordsAmount), joinLimit);
            allSubLists.addAll(flatMapWithLeadingAndTailing(leadingCombinations, joined, tailingCombinations));
        }
        return allSubLists;
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
}