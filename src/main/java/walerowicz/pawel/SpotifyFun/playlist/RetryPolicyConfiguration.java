package walerowicz.pawel.SpotifyFun.playlist;

import dev.failsafe.RetryPolicy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class RetryPolicyConfiguration {
    public <T> RetryPolicy<T> configureRetryPolicy(Class<? extends Throwable> exceptionClass) {
        return RetryPolicy.<T>builder()
                .handle(exceptionClass)
                .withDelay(5, 30, ChronoUnit.SECONDS)
                .withMaxRetries(5)
                .withMaxDuration(Duration.ofMinutes(3))
                .onRetry(listener ->
                        log.info("Too many requests send when creating playlist, waiting for a bit and retrying")
                )
                .build();
    }
}