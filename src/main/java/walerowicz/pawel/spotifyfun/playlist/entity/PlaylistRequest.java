package walerowicz.pawel.spotifyfun.playlist.entity;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlaylistRequest(
        @NotNull(message = "Playlist name is required")
        @NotEmpty(message = "Playlist name can't be empty")
        @Size(max = 150, message = "Playlist name can't be over 150 characters.")
        String name,

        @NotNull(message = "Request sentence is required")
        @NotEmpty(message = "Request sentence can't be empty")
        @Size(max = 500, message = "Request sentence can't be longer than 500 characters")
        String sentence,

        @NotNull(message = "Valid request token is required, please visit api/v1/auth/login to acquire one")
        String token
) {
}