package walerowicz.pawel.SpotifyFun;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ApplicationConfiguration {
    private static final String API_BASE_URI = "https://api.spotify.com/v1/";

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(API_BASE_URI)
                .defaultHeaders(header -> {
                    header.setContentType(MediaType.APPLICATION_JSON);
                })
                .build();
    }
}
