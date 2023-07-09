package walerowicz.pawel.spotifyfun.configuration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import walerowicz.pawel.spotifyfun.authorization.exception.AuthorizationException;
import walerowicz.pawel.spotifyfun.playlist.exception.TracksNotFoundException;
import walerowicz.pawel.spotifyfun.playlist.exception.TooManyRequestsException;

import java.util.List;

@RestControllerAdvice
public class ControllerExceptionHandler {
    private static final HttpHeaders DEFAULT_RESPONSE_HEADERS = setDefaultResponseHeaders();

    private static HttpHeaders setDefaultResponseHeaders() {
        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiCallProblem> missingBodyException() {
        return new ResponseEntity<>(
                new ApiCallProblem("Request must contain a body with playlist name, sentence to transform and valid authorization token."),
                DEFAULT_RESPONSE_HEADERS,
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ApiCallProblem>> invalidBodyException(final MethodArgumentNotValidException exception) {
        return new ResponseEntity<>(
                ApiCallProblemBuilder.fromBindException(exception),
                DEFAULT_RESPONSE_HEADERS,
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ApiCallProblem> authorizationException(final AuthorizationException exception) {
        return new ResponseEntity<>(
                ApiCallProblemBuilder.fromException(exception),
                DEFAULT_RESPONSE_HEADERS,
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ApiCallProblem> tooManyRequestsProblem() {
        return new ResponseEntity<>(
                new ApiCallProblem("Exceeded number of allowed calls to Spotify API. Please try again later."),
                DEFAULT_RESPONSE_HEADERS,
                HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(TracksNotFoundException.class)
    public ResponseEntity<ApiCallProblem> cantFoundCombinationsException() {
        return new ResponseEntity<>(
                new ApiCallProblem("Algorithm failed to find exact match for given sentence. " +
                        "Please check if it contains any misspelled words and/or consider shorter request."),
                DEFAULT_RESPONSE_HEADERS,
                HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiCallProblem> requestError(final MissingServletRequestParameterException exception) {
        return new ResponseEntity<>(
                ApiCallProblemBuilder.fromException(exception),
                DEFAULT_RESPONSE_HEADERS,
                HttpStatus.BAD_REQUEST);
    }

}
