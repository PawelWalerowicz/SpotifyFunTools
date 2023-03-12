package walerowicz.pawel.SpotifyFun.playlist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import walerowicz.pawel.SpotifyFun.playlist.entities.Track;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
class ConcurrentRequestProcessor {
    Logger logger = LoggerFactory.getLogger(ConcurrentRequestProcessor.class);
    private final ConcurrentSearchCallFactory concurrentSearchCallFactory;

    @Autowired
    ConcurrentRequestProcessor(ConcurrentSearchCallFactory concurrentSearchCallFactory) {
        this.concurrentSearchCallFactory = concurrentSearchCallFactory;
    }

    Map<String, List<Track>> sendConcurrentRequests(final List<String> allQueries) {
        List<Callable<Map<String, List<Track>>>> allCallables = prepareConcurrentRequests(allQueries);
        boolean shouldRetry = true;
        Map<String, List<Track>> result = null;
        int retryCount=0;
        do {        //Move it into individual request, not all of them
            try {
                result = sendRequests(allCallables);
                shouldRetry = false;
            } catch (TooManyRequestsException e1) {
                retryCount++;
                waitAndLog();
            }
        } while (shouldRetry && retryCount<10);
        return result;
    }

    private void waitAndLog() {
        logger.warn("Too many requests occurred. Taking a 30 second break");
        try {
            Thread.sleep(20_000);
            logger.warn("(just 10 more seconds)");
            Thread.sleep(10_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private List<Callable<Map<String, List<Track>>>> prepareConcurrentRequests(final List<String> allQueries) {
        return allQueries.stream()
                .map(concurrentSearchCallFactory::createConcurrentSearchCall)
                .collect(Collectors.toList());
    }

    private Map<String, List<Track>> sendRequests(final List<Callable<Map<String, List<Track>>>> callables) throws TooManyRequestsException {
        ExecutorService threadPool = Executors.newFixedThreadPool(callables.size());
        Map<String, List<Track>> titles = new HashMap<>();
        try {
            List<Future<Map<String, List<Track>>>> futures = threadPool.invokeAll(callables, 30, TimeUnit.SECONDS);
            for (Future<Map<String, List<Track>>> future : futures) {
                titles.putAll(future.get());
            }
        } catch (ExecutionException e) {
            throw new TooManyRequestsException();
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        }
        return titles;
    }
}
