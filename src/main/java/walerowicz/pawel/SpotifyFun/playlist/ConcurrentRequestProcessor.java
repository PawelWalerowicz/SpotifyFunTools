package walerowicz.pawel.SpotifyFun.playlist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import walerowicz.pawel.SpotifyFun.playlist.entities.TracksWithPhrase;

import java.util.ArrayList;
import java.util.List;
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

    List<TracksWithPhrase> sendConcurrentRequests(final List<String> allQueries) {
        List<Callable<TracksWithPhrase>> allCallables = prepareConcurrentRequests(allQueries);
        boolean shouldRetry = true;
        List<TracksWithPhrase> result = null;
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

    private List<Callable<TracksWithPhrase>> prepareConcurrentRequests(final List<String> allQueries) {
        return allQueries.stream()
                .map(concurrentSearchCallFactory::createConcurrentSearchCall)
                .collect(Collectors.toList());
    }

    private List<TracksWithPhrase> sendRequests(final List<Callable<TracksWithPhrase>> callables) throws TooManyRequestsException {
        ExecutorService threadPool = Executors.newFixedThreadPool(callables.size());
        List<TracksWithPhrase> titles = new ArrayList<>();
        try {
            List<Future<TracksWithPhrase>> futures = threadPool.invokeAll(callables, 30, TimeUnit.SECONDS);
            for (Future<TracksWithPhrase> future : futures) {
                titles.add(future.get());
            }
        } catch (ExecutionException e) {
            throw new TooManyRequestsException();
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        }
        return titles;
    }
}
