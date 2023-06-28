package walerowicz.pawel.SpotifyFun.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import walerowicz.pawel.SpotifyFun.authorization.AuthorizationException;
import walerowicz.pawel.SpotifyFun.playlist.combinations.CombinationNotFoundException;
import walerowicz.pawel.SpotifyFun.playlist.concurrent.search.TooManyRequestsException;

import java.util.List;

@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerExceptionHandler {
    private final HttpHeaders responseHeaders;

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiCallProblem> missingBodyProblem() {
        return new ResponseEntity<>(
                new ApiCallProblem("Request must contain a body with playlist name, sentence to transform and valid authorization token."),
                responseHeaders,
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ApiCallProblem>> missingBodyProblem(final MethodArgumentNotValidException exception) {
        return new ResponseEntity<>(
                ApiCallProblem.fromBindException(exception),
                responseHeaders,
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ApiCallProblem> unexpectedEncodingProblem(final AuthorizationException exception) {
        return new ResponseEntity<>(
                ApiCallProblem.fromException(exception),
                responseHeaders,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ApiCallProblem> tooManyRequestsProblem() {
        return new ResponseEntity<>(
                new ApiCallProblem("Exceeded number of allowed calls to SpotifyAPI. Please try again later."),
                responseHeaders,
                HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(CombinationNotFoundException.class)
    public ResponseEntity<ApiCallProblem> cantFoundCombinationsException() {
        return new ResponseEntity<>(
                new ApiCallProblem("This algorithm failed to find exact match for given sentence. " +
                        "Please check if it contains any misspelled words and/or consider shorter request."),
                responseHeaders,
                HttpStatus.UNPROCESSABLE_ENTITY);
    }

}
