package walerowicz.pawel.SpotifyFun.authorization.entites;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SpotifyAccessToken(
        @JsonProperty("access_token") String token,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("expires_in") int expirationTimeInSec,
        String scope
) {
}
