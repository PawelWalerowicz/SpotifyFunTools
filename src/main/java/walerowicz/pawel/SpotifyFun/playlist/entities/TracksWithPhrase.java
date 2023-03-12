package walerowicz.pawel.SpotifyFun.playlist.entities;

import java.util.List;

public record TracksWithPhrase(String phrase, List<Track> matchingTracks) implements Comparable<TracksWithPhrase> {
    public boolean hasMatchingTracks() {
        return !matchingTracks.isEmpty();
    }

    @Override
    public int compareTo(TracksWithPhrase o) {
        return this.matchingTracks.size() - o.matchingTracks.size();
    }
}
