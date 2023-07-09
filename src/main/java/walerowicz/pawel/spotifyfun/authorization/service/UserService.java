package walerowicz.pawel.spotifyfun.authorization.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import walerowicz.pawel.spotifyfun.authorization.exception.AuthorizationException;
import walerowicz.pawel.spotifyfun.authorization.entity.User;
import walerowicz.pawel.spotifyfun.playlist.exception.TooManyRequestsException;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final String GET_USER_ENDPOINT = "me";

    private final WebClient webClient;

    public User fetchUser(final String token) {
        return webClient
                .get()
                .uri(GET_USER_ENDPOINT)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .exchangeToMono(response -> {
                    final var httpStatusCode = response.statusCode();
                    if (httpStatusCode.equals(HttpStatus.TOO_MANY_REQUESTS)) {
                        return Mono.error(new TooManyRequestsException());
                    } else if (httpStatusCode.equals(HttpStatus.UNAUTHORIZED)) {
                        return Mono.error(new AuthorizationException("Web token expired"));
                    } else {
                        return response.bodyToMono(User.class);
                    }
                })
                .block();
    }
}