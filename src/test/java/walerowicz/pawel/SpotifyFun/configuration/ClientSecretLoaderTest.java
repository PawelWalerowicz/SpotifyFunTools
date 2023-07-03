package walerowicz.pawel.SpotifyFun.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.ResourceLoader;
import walerowicz.pawel.SpotifyFun.authorization.AuthorizationException;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientSecretLoaderTest {
    private ClientSecretLoader loader;

    @Mock
    ResourceLoader resourceLoader;

    @BeforeEach
    void setUp() {
        loader = new ClientSecretLoader(resourceLoader, "secretFileName");
    }

    @Test
    void shouldLoadResourceContentWhenResourceExists() {
        final var resourceContent = "Input content";
        when(resourceLoader.getResource("classpath:secretFileName"))
                .thenReturn(new InputStreamResource(new ByteArrayInputStream(resourceContent.getBytes(StandardCharsets.UTF_8))));
        final var result = loader.loadSpotifyClientSecret();
        assertEquals(resourceContent, result);
    }

    @Test
    void shouldCallResourceLoaderMethodWithClassPathPrefix() {
        final var resourceContent = "Input content";
        when(resourceLoader.getResource(any(String.class)))
                .thenReturn(new InputStreamResource(new ByteArrayInputStream(resourceContent.getBytes(StandardCharsets.UTF_8))));
        loader.loadSpotifyClientSecret();
        verify(resourceLoader).getResource("classpath:secretFileName");
    }

    @Test
    void shouldThrowAuthorizationExceptionWhenResourceDoesNotExist() {
        assertThrows(AuthorizationException.class, () -> loader.loadSpotifyClientSecret());
    }
}
