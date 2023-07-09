package walerowicz.pawel.spotifyfun.playlist.service.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import walerowicz.pawel.spotifyfun.playlist.entity.TracksWithPhrase;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class ConcurrentRequestProcessor {
    @Value("${spotify.combinator.cleanup.regex}")
    private final String cleanupRegex;
    private ConcurrentSearchFactory concurrentSearchFactory;
    private Set<ConcurrentSearch> concurrentSearches;

    public Set<TracksWithPhrase> sendConcurrentRequests(final List<String> allQueries,
                                       final String token) {
        final var outputSet = new CopyOnWriteArraySet<TracksWithPhrase>();
        this.concurrentSearchFactory = new ConcurrentSearchFactory(cleanupRegex, token, outputSet);
        prepareConcurrentRequests(allQueries);
        sendRequests();
        return outputSet;
    }

    public void stopSendingRequests() {
        concurrentSearches.forEach(ConcurrentSearch::shutDown);
        log.info("Request processor has stopped.");
    }

    private void prepareConcurrentRequests(final List<String> allQueries) {
        concurrentSearches = allQueries.stream()
                .map(concurrentSearchFactory::buildConcurrentSearch)
                .collect(Collectors.toSet());
    }

    private void sendRequests() {
        final var threadPool = Executors.newFixedThreadPool(concurrentSearches.size());
        concurrentSearches.forEach(threadPool::execute);
    }

}
