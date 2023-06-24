package walerowicz.pawel.SpotifyFun.playlist;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import walerowicz.pawel.SpotifyFun.playlist.entities.TracksWithPhrase;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Slf4j
@Scope("prototype")
class ConcurrentRequestProcessor {
    private final ConcurrentSearchFactory concurrentSearchFactory;
    private ExecutorService threadPool;
    private Set<ConcurrentSearch> concurrentSearches;

    @Autowired
    ConcurrentRequestProcessor(ConcurrentSearchFactory concurrentSearchCallFactory) {
        this.concurrentSearchFactory = concurrentSearchCallFactory;
    }

    void sendConcurrentRequests(final List<String> allQueries,
                                final String token,
                                final Set<TracksWithPhrase> outputSet) {
        prepareConcurrentRequests(allQueries, token, outputSet);
        sendRequests();
    }

    void stopSendingRequests() {
        concurrentSearches.forEach(ConcurrentSearch::shutDown);
        log.info("Request processor has stopped.");
    }

    private void prepareConcurrentRequests(final List<String> allQueries,
                                           final String token,
                                           final Set<TracksWithPhrase> outputSet) {
        concurrentSearches = allQueries.stream()
                .map(query -> concurrentSearchFactory.createConcurrentSearchInstance(query, token, outputSet))
                .collect(Collectors.toSet());
    }

    private void sendRequests() {
        threadPool = Executors.newFixedThreadPool(concurrentSearches.size());
        concurrentSearches.forEach(threadPool::execute);
    }

}
