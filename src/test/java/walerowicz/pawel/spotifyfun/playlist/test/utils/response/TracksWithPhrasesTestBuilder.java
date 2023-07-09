package walerowicz.pawel.spotifyfun.playlist.test.utils.response;

import walerowicz.pawel.spotifyfun.playlist.entity.Track;
import walerowicz.pawel.spotifyfun.playlist.entity.TracksWithPhrase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TracksWithPhrasesTestBuilder {
    private List<TracksWithPhrase> phrases = defaultPhrases();
    private int counter;

    private TracksWithPhrasesTestBuilder() {
        this.counter = 1;
    }

    private List<TracksWithPhrase> defaultPhrases() {
        return List.of(new TracksWithPhrase("Phrase_1", List.of(new Track("id1", "href", "Phrase_1"))));
    }

    public static TracksWithPhrasesTestBuilder newBuilder() {
        return new TracksWithPhrasesTestBuilder();
    }

    public TracksWithPhrasesTestBuilder withPhraseWithMatchingTracks(final String phrase, final int matchingTracksAmount) {
        withPhraseWithMatchingTracks(phrase, matchingTracksAmount, 0);
        return this;
    }

    public TracksWithPhrasesTestBuilder withPhraseWithMatchingTracks(final String phrase,
                                                                     final int exactMatchesAmount,
                                                                     final int notExactMatchesAmount
    ) {
        if (this.phrases.equals(defaultPhrases())) {
            this.phrases = new ArrayList<>();
        }
        final var tracks = new ArrayList<Track>();
        for (int i = 0; i < exactMatchesAmount; i++) {
            tracks.add(new Track(phrase + "_id_" + counter, "href" + counter, phrase));
            counter++;
        }
        for (int i = 0; i < notExactMatchesAmount; i++) {
            tracks.add(new Track(phrase + "_id_" + counter, "href" + counter, phrase + " not matching title" + counter));
            counter++;
        }
        phrases.add(new TracksWithPhrase(phrase, tracks));
        return this;
    }

    public List<TracksWithPhrase> buildAsList() {
        return this.phrases;
    }

    public Set<TracksWithPhrase> buildAsSet() {
        return new HashSet<>(this.phrases);
    }


}
