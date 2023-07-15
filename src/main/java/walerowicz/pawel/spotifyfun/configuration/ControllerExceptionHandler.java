package walerowicz.pawel.spotifyfun.configuration;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import walerowicz.pawel.spotifyfun.authorization.exception.AuthorizationException;
import walerowicz.pawel.spotifyfun.playlist.exception.TooManyRequestsException;
import walerowicz.pawel.spotifyfun.playlist.exception.TracksNotFoundException;

import java.util.List;

@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiCallProblem missingBodyException() {
        return new ApiCallProblem("Request must contain a body with playlist name, " +
                "sentence to transform and valid authorization token.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public List<ApiCallProblem> invalidBodyException(final MethodArgumentNotValidException exception) {
        return ApiCallProblemBuilder.fromBindException(exception);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiCallProblem requestError(final MissingServletRequestParameterException exception) {
        return ApiCallProblemBuilder.fromException(exception);
    }

    @ExceptionHandler(AuthorizationException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ApiCallProblem authorizationException(final AuthorizationException exception) {
        return ApiCallProblemBuilder.fromException(exception);
    }

    @ExceptionHandler(TooManyRequestsException.class)
    @ResponseStatus(value = HttpStatus.TOO_MANY_REQUESTS)
    public ApiCallProblem tooManyRequestsProblem() {
        return new ApiCallProblem("Exceeded number of allowed calls to Spotify API. Please try again later.");
    }

    @ExceptionHandler(TracksNotFoundException.class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiCallProblem cantFoundCombinationsException() {
        return new ApiCallProblem("Algorithm failed to find exact match for given sentence. " +
                "Please check if it contains any misspelled words and/or consider shorter request.");
    }

}
