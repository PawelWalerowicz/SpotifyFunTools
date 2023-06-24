package walerowicz.pawel.SpotifyFun.playlist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import walerowicz.pawel.SpotifyFun.playlist.entities.TracksWithPhrase;

import java.util.Set;

@Service
class ConcurrentSearchFactory {
    private final String cleanupRegex;

    @Autowired
    ConcurrentSearchFactory(@Value("${spotify.combinator.cleanup.regex}") final String cleanupRegex) {
        this.cleanupRegex = cleanupRegex;
    }

    ConcurrentSearch createConcurrentSearchInstance(final String query,
                                                    final String token,
                                                    final Set<TracksWithPhrase> outputSet) {
        return new ConcurrentSearch(
                query,
                cleanupRegex,
                token,
                outputSet
        );
    }
}