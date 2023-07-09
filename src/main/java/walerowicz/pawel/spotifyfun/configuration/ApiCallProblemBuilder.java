package walerowicz.pawel.spotifyfun.configuration;

import org.springframework.validation.BindException;

import java.util.List;

public class ApiCallProblemBuilder {
    public static ApiCallProblem fromException(Exception exception) {
        return new ApiCallProblem(exception.getMessage());
    }

    public static List<ApiCallProblem> fromBindException(final BindException bindException) {
        return bindException.getBindingResult().getFieldErrors()
                .stream().map(error -> new ApiCallProblem(error.getDefaultMessage()))
                .toList();
    }
}
