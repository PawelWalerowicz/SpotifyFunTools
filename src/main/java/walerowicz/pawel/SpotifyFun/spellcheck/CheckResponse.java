package walerowicz.pawel.SpotifyFun.spellcheck;

import java.util.List;

record CheckResponse(List<Element> elements, int spellingErrorCount) {
}
