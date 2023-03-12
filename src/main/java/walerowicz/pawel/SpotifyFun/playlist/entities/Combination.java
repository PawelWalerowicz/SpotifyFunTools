package walerowicz.pawel.SpotifyFun.playlist.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Combination {
    private final List<String> phraseList;

    public Combination(final List<String> phraseList) {
        this.phraseList = phraseList;
    }

    public Combination(final Combination leadingPhrases, final String centralPhrase, final Combination tailingPhrases) {
        this.phraseList = new ArrayList<>();
        this.phraseList.addAll(leadingPhrases.getPhraseList());
        this.phraseList.add(centralPhrase);
        this.phraseList.addAll(tailingPhrases.getPhraseList());
    }

    public List<String> getPhraseList() {
        return phraseList;
    }

    public int getSize() {
        return phraseList.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Combination that = (Combination) o;
        return Objects.equals(phraseList, that.phraseList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phraseList);
    }

    @Override
    public String toString() {
        return "Combination{" +
                "phraseList=" + phraseList +
                '}';
    }
}
