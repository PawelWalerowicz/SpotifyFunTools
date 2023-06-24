package walerowicz.pawel.SpotifyFun.authorization;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import walerowicz.pawel.SpotifyFun.ApiCallProblem;

@RestControllerAdvice("api/v1/auth")
@RequiredArgsConstructor
public class AuthorizationExceptionHandler {
    private final HttpHeaders responseHeaders;

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ApiCallProblem> unexpectedEncodingProblem(final AuthorizationException exception) {
        return new ResponseEntity<>(ApiCallProblem.fromException(exception), responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
