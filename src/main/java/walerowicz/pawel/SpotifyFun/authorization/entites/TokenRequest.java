package walerowicz.pawel.SpotifyFun.authorization.entites;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

public record TokenRequest(@JsonProperty("grant_type") String grantType,
                           @JsonProperty("code") String code,
                           @JsonProperty("redirect_uri") String redirectUri) {
    public MultiValueMap<String, String> toMultiValueMap() {
        var bodyValues = new LinkedMultiValueMap<String, String>();
        bodyValues.put("grant_type", List.of(this.grantType));
        bodyValues.put("code", List.of(this.code));
        bodyValues.put("redirect_uri", List.of(this.redirectUri));
        return bodyValues;
    }
}