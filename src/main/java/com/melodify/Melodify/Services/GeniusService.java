package com.melodify.Melodify.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.melodify.Melodify.Config.RestTemplateConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeniusService {
    private static final String GENIUS_API_KEY = RestTemplateConfig.GENIUS_API_KEY;
    private static final String GENIUS_SEARCH_URL = "https://api.genius.com/search";

    private final RestTemplate restTemplate;

    @Autowired
    public GeniusService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    //Search for Albums, Artists, Tracks via Genius API
    public List<Map<String, String>> search(String query) {
        String url = GENIUS_SEARCH_URL + "?q=" + query;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + GENIUS_API_KEY);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<JsonNode>() {
                }
        );

        List<Map<String, String>> results = new ArrayList<>();
        JsonNode hits = response.getBody().path("response").path("hits");

        for (JsonNode hit : hits) {
            JsonNode result = hit.path("result");
            Map<String, String> item = new LinkedHashMap<>();
            item.put("name", result.path("title").asText());
            item.put("artist", result.path("primary_artist").path("name").asText());
            item.put("image", result.path("song_art_image_thumbnail_url").asText());
            item.put("type", "track");
            results.add(item);
        }

        return results;
    }
}
