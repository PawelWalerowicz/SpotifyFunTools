package walerowicz.pawel.spotifyfun.playlist.service.combinations;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import walerowicz.pawel.spotifyfun.playlist.entity.Combination;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WordCombiner {
    @Value("${spotify.combinator.words.limit}")
    private final int joinLimit;
    @Value("${spotify.combinator.cleanup.regex}")
    private final String cleanUpRegex;

    //when joinLimit >= <single words in input sentence> expected amount of combinations equals to 2^(<amount of words> -1)
    List<Combination> buildCombinations(final String inputSentence) {
        final var strippedAccents = StringUtils.stripAccents(inputSentence);
        final var singleWords = splitSentence(strippedAccents);
        return combine(singleWords);
    }

    List<String> distinctQueries(final List<Combination> combinedWords) {
        return combinedWords.stream()
                .map(Combination::getPhraseList)
                .flatMap(Collection::stream)
                .distinct()
                .toList();
    }

    private List<String> splitSentence(final String inputSentence) {
        return Arrays.stream(inputSentence.split(cleanUpRegex))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
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
        final var allSubLists = new ArrayList<Combination>();
        final var allWordsAmount = allWords.size();
        for (var startIndex = 0; startIndex < allWordsAmount + 1 - joinedWords; startIndex++) {
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
        final var solvedCombinations = new ArrayList<Combination>();
        for (var leadingCombination : leadingCombinations) {
            for (Combination tailingCombination : tailingCombinations) {
                solvedCombinations.add(new Combination(leadingCombination, joined, tailingCombination));
            }
        }
        return solvedCombinations;
    }
}