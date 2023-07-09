package walerowicz.pawel.spotifyfun.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ApiCallProblem(
        @JsonProperty("Error message") String message
) {
}
