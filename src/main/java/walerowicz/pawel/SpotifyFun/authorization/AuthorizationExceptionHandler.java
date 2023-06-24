package walerowicz.pawel.SpotifyFun.authorization;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import walerowicz.pawel.SpotifyFun.ApiCallProblem;

@RestControllerAdvice("api/v1/auth")
public class AuthorizationExceptionHandler {
    private final HttpHeaders responseHeaders = initializeHeaders();

    private HttpHeaders initializeHeaders() {
        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ApiCallProblem> unexpectedEncodingProblem(final AuthorizationException exception) {
        return new ResponseEntity<>(ApiCallProblem.fromException(exception), responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
