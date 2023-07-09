package walerowicz.pawel.spotifyfun.playlist.entity;

import com.fasterxml.jackson.annotation.JsonAlias;

public record FetchTracksResult(
        @JsonAlias("tracks") FoundTracksResultPackage foundTracksResultPackage
) {}
