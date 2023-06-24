package walerowicz.pawel.SpotifyFun.playlist.entities;

import com.fasterxml.jackson.annotation.JsonAlias;

public record FetchTracksResult(@JsonAlias("tracks") FoundTracksResultPackage foundTracksResultPackage) {}
