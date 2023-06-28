package walerowicz.pawel.SpotifyFun.playlist.concurrent.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import walerowicz.pawel.SpotifyFun.playlist.entities.TracksWithPhrase;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class ConcurrentRequestProcessor {
    @Value("${spotify.combinator.cleanup.regex}")
    private final String cleanupRegex;
    private Set<ConcurrentSearch> concurrentSearches;

    public void sendConcurrentRequests(final List<String> allQueries,
                                       final String token,
                                       final Set<TracksWithPhrase> outputSet) {
        prepareConcurrentRequests(allQueries, token, outputSet);
        sendRequests();
    }

    public void stopSendingRequests() {
        concurrentSearches.forEach(ConcurrentSearch::shutDown);
        log.info("Request processor has stopped.");
    }

    private void prepareConcurrentRequests(final List<String> allQueries,
                                           final String token,
                                           final Set<TracksWithPhrase> outputSet) {
        concurrentSearches = allQueries.stream()
                .map(query -> new ConcurrentSearch(query, token, cleanupRegex, outputSet))
                .collect(Collectors.toSet());
    }

    private void sendRequests() {
        final var threadPool = Executors.newFixedThreadPool(concurrentSearches.size());
        concurrentSearches.forEach(threadPool::execute);
    }

}
