package walerowicz.pawel.SpotifyFun.authorization.entites;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

@RequiredArgsConstructor
public class TokenRequest {
    @JsonProperty("grant_type")
    private final String grantType;
    @JsonProperty("code")
    private final String code;
    @JsonProperty("redirect_uri")
    private final String redirectUri;

    public MultiValueMap<String, String> toMultiValueMap() {
        var bodyValues = new LinkedMultiValueMap<String, String>();
        bodyValues.put("grant_type", List.of(this.grantType));
        bodyValues.put("code", List.of(this.code));
        bodyValues.put("redirect_uri", List.of(this.redirectUri));
        return bodyValues;
    }
}