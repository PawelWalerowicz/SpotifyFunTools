package walerowicz.pawel.spotifyfun.playlist.service.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import walerowicz.pawel.spotifyfun.playlist.entity.TracksWithPhrase;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
    private ExecutorService executorService;

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
        try {
            if (Objects.nonNull(executorService)) {
                executorService.awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error("Thread interrupted during executor service termination");
            Thread.currentThread().interrupt();
        }
        log.info("Request processor has stopped.");
    }

    private void prepareConcurrentRequests(final List<String> allQueries) {
        concurrentSearches = allQueries.stream()
                .map(concurrentSearchFactory::buildConcurrentSearch)
                .collect(Collectors.toSet());
    }

    private void sendRequests() {
        executorService = Executors.newFixedThreadPool(concurrentSearches.size());
        concurrentSearches.forEach(executorService::execute);
    }

}
