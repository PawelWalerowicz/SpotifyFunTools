package walerowicz.pawel.spotifyfun.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ApplicationConfiguration {
    private static final String API_BASE_URI = "https://api.spotify.com/v1/";
    private static final String TOKEN_URI = "https://accounts.spotify.com/api/token";

    @Bean
    @Primary
    public WebClient apiWebClient() {
        return WebClient.builder()
                .baseUrl(API_BASE_URI)
                .defaultHeaders(header -> header.setContentType(MediaType.APPLICATION_JSON))
                .build();
    }
    @Bean("account")
    public WebClient accountWebClient() {
        return WebClient.builder()
                .baseUrl(TOKEN_URI)
                .defaultHeaders(header -> header.setContentType(MediaType.APPLICATION_FORM_URLENCODED))
                .build();
    }

    @Bean
    public HttpHeaders defaultResponseHeaders() {
        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
