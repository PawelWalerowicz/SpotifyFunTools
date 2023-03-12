package walerowicz.pawel.SpotifyFun.playlist.entities;

import com.fasterxml.jackson.annotation.JsonAlias;

public record SearchResult(@JsonAlias("tracks") FoundTracksResultPackage foundTracksResultPackage) {}
