package walerowicz.pawel.SpotifyFun.playlist;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import walerowicz.pawel.SpotifyFun.playlist.entities.Combination;

import java.util.*;
import java.util.stream.Collectors;

@Service
class WordCombiner {
    private final int joinLimit;
    private final String cleanUpRegex;

    public WordCombiner(@Value("${spotify.combinator.words.limit}") final int joinLimit,
                        @Value("${spotify.combinator.cleanup.regex}") final String cleanUpRegex) {
        this.joinLimit = joinLimit;
        this.cleanUpRegex = cleanUpRegex;
    }

    List<Combination> buildCombinations(final String inputSentence) {
        List<String> singleWords = splitSentence(inputSentence);
        return combine(singleWords);
    }

    List<String> distinctQueries(final List<Combination> combinedWords) {
        return combinedWords.stream()
                .map(Combination::getPhraseList)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> splitSentence(final String inputSentence) {
        return Arrays.stream(inputSentence.split(cleanUpRegex))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private List<Combination> combine(final List<String> allWords) {
        List<Combination> allCombinations = new ArrayList<>();
        allCombinations.add(new Combination(allWords));
        for (int joinedWords = 2; joinedWords <= Math.min(joinLimit, allWords.size()); joinedWords++) {
            allCombinations.addAll(combineForJoinedWords(joinedWords, allWords));
        }
        allCombinations = allCombinations.stream().distinct().collect(Collectors.toList());
        Collections.reverse(allCombinations);
        return allCombinations;
    }

    private List<Combination> combineForJoinedWords(final int joinedWords, final List<String> allWords) {
        final List<Combination> allSubLists = new ArrayList<>();
        final int allWordsAmount = allWords.size();
        for (int startIndex = 0; startIndex < allWordsAmount + 1 - joinedWords; startIndex++) {
            int endIndex = startIndex + joinedWords;
            final List<Combination> leadingCombinations = combine(allWords.subList(0, startIndex));
            final String joined = String.join(" ", allWords.subList(startIndex, endIndex));
            final List<Combination> tailingCombinations = combine(allWords.subList(endIndex, allWordsAmount));
            allSubLists.addAll(flatMapWithLeadingAndTailing(leadingCombinations, joined, tailingCombinations));
        }
        return allSubLists;
    }

    private List<Combination> flatMapWithLeadingAndTailing(final List<Combination> leadingCombinations,
                                                           final String joined,
                                                           final List<Combination> tailingCombinations) {
        final List<Combination> solvedCombinations = new ArrayList<>();
        for (Combination leadingCombination : leadingCombinations) {
            for (Combination tailingCombination : tailingCombinations) {
                solvedCombinations.add(new Combination(leadingCombination, joined, tailingCombination));
            }
        }
        return solvedCombinations;
    }
}