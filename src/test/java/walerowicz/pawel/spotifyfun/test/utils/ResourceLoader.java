package walerowicz.pawel.spotifyfun.test.utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

public class ResourceLoader {
    @NotNull
    public static String getResource(final String fileName) {
        try (final var resourceAsStream = ResourceLoader.class
                .getClassLoader()
                .getResourceAsStream(fileName)
        ) {
            final var bytes = Objects.requireNonNull(resourceAsStream).readAllBytes();
            return new String(bytes);
        } catch (IOException e) {
            return "Exception while getting test resource";
        }
    }
}
