package walerowicz.pawel.SpotifyFun.spellcheck;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import walerowicz.pawel.SpotifyFun.ClientSecretLoader;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SpellCheck {
    private final ClientSecretLoader secretLoader;
    private final BodyFactory bodyFactory;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final String secretFilename;
    private final String requestURI;
    private final String apiHost;

    @Autowired
    public SpellCheck(final ClientSecretLoader secretLoader,
                      final BodyFactory bodyFactory,
                      @Value("${spellcheck.secret.filename}") final String secretFilename,
                      @Value("${spellcheck.checkURL}") final String requestURI,
                      @Value("${spellcheck.host}") final String apiHost) {
        this.secretLoader = secretLoader;
        this.bodyFactory = bodyFactory;
        this.objectMapper = new ObjectMapper();
        this.restTemplate = new RestTemplate();
        this.secretFilename = secretFilename;
        this.requestURI = requestURI;
        this.apiHost = apiHost;
    }

    public String correctSpelling(final String inputSentence) {
        try {
            final var checkResponse = sendCheckSpellingRequest(inputSentence);
            System.out.println(checkResponse);
            if (checkResponse != null && checkResponse.elements() !=null) {
                return replaceMistakes(inputSentence, checkResponse);
            }
        } catch (JsonProcessingException | URISyntaxException e) {
            log.warn("Spell checker failed. Returning original sentence", e);
        }
        return inputSentence;
    }

    private CheckResponse sendCheckSpellingRequest(final String inputSentence) throws JsonProcessingException, URISyntaxException {
        final var defaultBody = bodyFactory.createDefaultBody(inputSentence);
        final var httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(defaultBody), buildRequestHeader());
        final var exchange = restTemplate.exchange(new URI(requestURI), HttpMethod.POST, httpEntity, CheckResponse.class);
        return exchange.getBody();
    }

    private HttpHeaders buildRequestHeader() {
        final HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.set("X-RapidAPI-Key", secretLoader.loadClientSecret(secretFilename));
        header.set("X-RapidAPI-Host", apiHost);
        return header;
    }

    private String replaceMistakes(String inputSentence, final CheckResponse checkResponse) {
        for (Element element : checkResponse.elements()) {
            for (Error error : element.errors()) {
                final var misspelledWord = error.word();
                final var replacement = chooseMostSimilarSuggestion(misspelledWord, error.suggestions());
                inputSentence = inputSentence.replace(misspelledWord, replacement);
            }
        }
        return inputSentence;
    }

    private String chooseMostSimilarSuggestion(String misspelledWord, List<String> suggestions) {
        Map<Integer, String> differencesRanking = new HashMap<>();
        for (String suggestion : suggestions) {
            int differences = Math.abs(misspelledWord.length() - suggestion.length());
            for(int i=0; i<Math.min(misspelledWord.length(), suggestion.length()); i++) {
                if(misspelledWord.charAt(i) != suggestion.charAt(i)) {
                    differences++;
                }
            }
            differencesRanking.put(differences, suggestion);
        }
        return differencesRanking.keySet()
                .stream()
                .sorted()
                .map(differencesRanking::get)
                .findFirst()
                .orElse(misspelledWord);
    }
}