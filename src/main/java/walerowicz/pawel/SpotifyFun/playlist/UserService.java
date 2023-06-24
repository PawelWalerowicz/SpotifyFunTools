package walerowicz.pawel.SpotifyFun.playlist;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import walerowicz.pawel.SpotifyFun.authorization.AuthorizationException;
import walerowicz.pawel.SpotifyFun.authorization.entites.User;
import walerowicz.pawel.SpotifyFun.playlist.concurrent.TooManyRequestsException;

@Service
@RequiredArgsConstructor
class UserService {
    private static final String GET_USER_ENDPOINT = "me";

    private final WebClient webClient;

    User importUser(String token) {
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