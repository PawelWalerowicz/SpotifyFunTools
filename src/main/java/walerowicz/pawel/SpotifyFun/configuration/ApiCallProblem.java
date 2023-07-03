package walerowicz.pawel.SpotifyFun.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.validation.BindException;

import java.util.List;
import java.util.stream.Collectors;

public record ApiCallProblem(
        @JsonProperty("Error message") String message
) {
    public static ApiCallProblem fromException(Exception exception) {
        return new ApiCallProblem(exception.getMessage());
    }
    public static List<ApiCallProblem> fromBindException(final BindException bindException) {
        return bindException.getBindingResult().getFieldErrors()
                .stream().map(error -> new ApiCallProblem(error.getDefaultMessage()))
                .collect(Collectors.toList());
    }
}
