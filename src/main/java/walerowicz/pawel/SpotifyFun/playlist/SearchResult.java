package walerowicz.pawel.SpotifyFun.playlist;

import com.fasterxml.jackson.annotation.JsonAlias;

record SearchResult(@JsonAlias("tracks") FoundTracksResultPackage foundTracksResultPackage) {}
