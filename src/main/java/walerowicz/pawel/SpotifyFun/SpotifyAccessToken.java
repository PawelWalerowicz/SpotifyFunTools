package walerowicz.pawel.SpotifyFun;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.time.LocalDateTime;

@Data
class SpotifyAccessToken {
    @JsonAlias("access_token")
    String token;

    @JsonAlias("expires_in")
    int expirationTimeInSec;

    LocalDateTime creationTimestamp;

    boolean isNotExpired() {
        return creationTimestamp.plusSeconds(expirationTimeInSec).isAfter(LocalDateTime.now());
    }
}