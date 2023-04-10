package walerowicz.pawel.SpotifyFun.playlist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import walerowicz.pawel.SpotifyFun.playlist.entities.TracksWithPhrase;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
class ConcurrentRequestProcessor {
    private final Logger logger = LoggerFactory.getLogger(ConcurrentRequestProcessor.class);
    private final ConcurrentSearchFactory concurrentSearchFactory;
    private ExecutorService threadPool;

    @Autowired
    ConcurrentRequestProcessor(ConcurrentSearchFactory concurrentSearchCallFactory) {
        this.concurrentSearchFactory = concurrentSearchCallFactory;
    }

    void sendConcurrentRequests(final List<String> allQueries, final Set<TracksWithPhrase> outputSet) {
        final var allSearches = prepareConcurrentRequests(allQueries, outputSet);
        sendRequests(allSearches);
    }

    void stopSendingRequests() {
        threadPool.shutdownNow();
        logger.info("Request processor has stopped.");
    }

    private Set<ConcurrentSearch> prepareConcurrentRequests(final List<String> allQueries, final Set<TracksWithPhrase> outputSet) {
        return allQueries.stream()
                .map(query -> concurrentSearchFactory.createConcurrentSearchInstance(query, outputSet))
                .collect(Collectors.toSet());
    }

    private void sendRequests(final Set<ConcurrentSearch> concurrentSearches) {
        threadPool = Executors.newFixedThreadPool(concurrentSearches.size());
        concurrentSearches.forEach(threadPool::execute);
    }

}
