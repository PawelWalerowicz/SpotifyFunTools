package walerowicz.pawel.spotifyfun.playlist.service.search;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Sleeper {
    private Sleeper() {
    }

    public static void sleep(final int waitPeriodMs) {
        try {
            Thread.sleep(waitPeriodMs);
        } catch (InterruptedException e) {
            log.error("Thread interrupted during wait period between checks", e);
            Thread.currentThread().interrupt();
        }
    }
}
