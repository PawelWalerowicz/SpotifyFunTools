package walerowicz.pawel.SpotifyFun.playlist;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import walerowicz.pawel.SpotifyFun.playlist.entities.TracksWithPhrase;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
class ConcurrentRequestProcessor {
    private final Logger logger = LoggerFactory.getLogger(ConcurrentRequestProcessor.class);
    private final ConcurrentSearchCallFactory concurrentSearchCallFactory;
    private final RetryPolicy<List<TracksWithPhrase>> retryPolicy;

    @Autowired
    ConcurrentRequestProcessor(ConcurrentSearchCallFactory concurrentSearchCallFactory) {
        this.concurrentSearchCallFactory = concurrentSearchCallFactory;
        this.retryPolicy = configureRetryPolicy();
    }

    List<TracksWithPhrase> sendConcurrentRequests(final List<String> allQueries) {
        final var allCallables = prepareConcurrentRequests(allQueries);
        return Failsafe.with(retryPolicy)
                .get(() -> sendRequests(allCallables));
    }

    private List<Callable<TracksWithPhrase>> prepareConcurrentRequests(final List<String> allQueries) {
        return allQueries.stream()
                .map(concurrentSearchCallFactory::createConcurrentSearchCall)
                .collect(Collectors.toList());
    }

    private List<TracksWithPhrase> sendRequests(final List<Callable<TracksWithPhrase>> callables) throws TooManyRequestsException {
        var threadPool = Executors.newFixedThreadPool(callables.size());
        var titles = new ArrayList<TracksWithPhrase>();
        try {
            var futures = threadPool.invokeAll(callables, 30, TimeUnit.SECONDS);
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

    private RetryPolicy<List<TracksWithPhrase>> configureRetryPolicy() {
        return RetryPolicy.<List<TracksWithPhrase>>builder()
                .handle(TooManyRequestsException.class)
                .withDelay(10, 30, ChronoUnit.SECONDS)
                .withMaxRetries(10)
                .withMaxDuration(Duration.ofMinutes(5))
                .onFailedAttempt(listener ->
                        logger.warn("Too many requests send, waiting for a bit, attempt took: {} s",
                                    listener.getElapsedAttemptTime().toSeconds()
                        )
                )
                .build();
    }
}
