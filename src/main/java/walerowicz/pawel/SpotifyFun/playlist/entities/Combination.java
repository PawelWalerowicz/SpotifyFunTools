package walerowicz.pawel.SpotifyFun.playlist.entities;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Combination implements Comparable<Combination> {
    private final List<String> phraseList;

    public Combination(final List<String> phraseList) {
        this.phraseList = phraseList;
    }

    public Combination(final Combination leadingPhrases,
                       final String centralPhrase,
                       final Combination tailingPhrases) {
        this.phraseList = new ArrayList<>();
        this.phraseList.addAll(leadingPhrases.getPhraseList());
        this.phraseList.add(centralPhrase);
        this.phraseList.addAll(tailingPhrases.getPhraseList());
    }

    @Override
    public int compareTo(Combination other) {
        return this.phraseList.size() - other.phraseList.size();
    }
}
