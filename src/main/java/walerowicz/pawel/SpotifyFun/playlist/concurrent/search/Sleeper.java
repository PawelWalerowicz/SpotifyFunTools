package walerowicz.pawel.SpotifyFun.playlist.concurrent.search;

class Sleeper {

    void sleep(final int waitPeriodMs) {
        try {
            Thread.sleep(waitPeriodMs);
        } catch (InterruptedException ignored) {
        }
    }
}
