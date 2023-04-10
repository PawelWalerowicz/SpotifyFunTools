package walerowicz.pawel.SpotifyFun.spellcheck;

import org.springframework.stereotype.Service;

@Service
class BodyFactory {
    private final static String LANGUAGE = "enUS";

    JSpellCheckerBody createDefaultBody(final String sentence) {
        JspellBodyConfig config = new JspellBodyConfig(
                false,
                true,
                true,
                true,
                true,
                true,
                true);
        return new JSpellCheckerBody(LANGUAGE, sentence, config);
    }


    record JspellBodyConfig(boolean forceUpperCase,
                            boolean ignoreIrregularCaps,
                            boolean ignoreFirstCaps,
                            boolean ignoreNumbers,
                            boolean ignoreUpper,
                            boolean ignoreDouble,
                            boolean ignoreWordsWithNumbers) {}
}
