package walerowicz.pawel.SpotifyFun.spellcheck;

import java.util.List;

record Error(String word,
             int position,
             List<String> suggestions) {
}
