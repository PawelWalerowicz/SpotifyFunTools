package walerowicz.pawel.SpotifyFun.authorization.entites;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SpotifyAccessToken {
    @JsonProperty("access_token")
    private String token;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expires_in")
    private int expirationTimeInSec;

    private String scope;

    private LocalDateTime creationTimestamp;

    public boolean isNotExpired() {
        return creationTimestamp.plusSeconds(expirationTimeInSec).isAfter(LocalDateTime.now());
    }
}