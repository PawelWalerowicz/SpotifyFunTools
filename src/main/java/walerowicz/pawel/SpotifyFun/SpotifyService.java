package walerowicz.pawel.SpotifyFun;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import walerowicz.pawel.SpotifyFun.dto.SearchResult;
import walerowicz.pawel.SpotifyFun.dto.Track;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SpotifyService {
    private final TokenRetriever tokenRetriever;

    @Autowired
    public SpotifyService(TokenRetriever tokenRetriever) {
        this.tokenRetriever = tokenRetriever;
    }

    void buildPlaylist(final String inputSentence) {
        List<String> singleWords = Arrays.stream(inputSentence.split("[ .-]"))
                .map(String::trim)
                .collect(Collectors.toList());
        System.out.println(singleWords);
//        List<List<String>> permutations = permuteList(singleWords);
        Map<String, List<String>> titles = new HashMap<>();
        for(String singleWord:singleWords) {
            List<String> matches = getTracks(singleWord).getFoundTracksResultPackage().getFoundTracks().stream().map(Track::getName).filter(name -> name.equalsIgnoreCase(singleWord)).collect(Collectors.toList());
            if(matches.isEmpty()) //ask again with nextHrefURL
            titles.put(singleWord, matches);
            System.out.println( singleWord + ": " + titles.get(singleWord));
        }

    }

//    private List<List<String>> permuteList(List<String> singleWords) {
//        List<List<String>> allPermutations = new ArrayList<>();
//        int length = singleWords.size();
//
//        ArrayList<Object> singlePermutation = new ArrayList<>();
//        for (int i = 0; i < length; i++) {
//            singleWords.get(i)
//            allPermutations.add()
//        }
//        return allPermutations;
//    }

    SearchResult getTracks(String query) {
        String url = "https://api.spotify.com/v1/search?q=" + query + "&type=track";
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> objectHttpEntity = new HttpEntity<>(null, buildRequestHeader());
//        System.out.println(restTemplate.exchange(url, HttpMethod.GET, objectHttpEntity, String.class).getBody());
        final ResponseEntity<SearchResult> responseEntity = restTemplate.exchange(url, HttpMethod.GET, objectHttpEntity, SearchResult.class);
        return responseEntity.getBody();
    }

    private HttpHeaders buildRequestHeader() {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.set("Authorization", "Bearer " + tokenRetriever.retrieveToken().getToken());
        return header;
    }
}
