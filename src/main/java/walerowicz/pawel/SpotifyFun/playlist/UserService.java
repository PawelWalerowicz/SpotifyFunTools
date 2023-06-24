package walerowicz.pawel.SpotifyFun.playlist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import walerowicz.pawel.SpotifyFun.authorization.entites.User;

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
                .retrieve()
                .bodyToMono(User.class)
                .block();
    }
}