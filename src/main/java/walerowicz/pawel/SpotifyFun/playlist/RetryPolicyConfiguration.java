package walerowicz.pawel.SpotifyFun.playlist;

import dev.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Service
public class RetryPolicyConfiguration {
    private final Logger logger = LoggerFactory.getLogger("RetryLog");
    public <T> RetryPolicy<T> configureRetryPolicy(Class<? extends Throwable> exceptionClass) {
        return RetryPolicy.<T>builder()
                .handle(exceptionClass)
                .withDelay(5, 30, ChronoUnit.SECONDS)
                .withMaxRetries(5)
                .withMaxDuration(Duration.ofMinutes(3))
                .onRetry(listener ->
                        logger.info("Too many requests send when creating playlist, waiting for a bit and retrying")
                )
                .build();
    }
}