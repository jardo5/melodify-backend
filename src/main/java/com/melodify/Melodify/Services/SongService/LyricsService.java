package com.melodify.Melodify.Services.SongService;

import com.fasterxml.jackson.databind.JsonNode;
import com.melodify.Melodify.Config.RestTemplateConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LyricsService {

    private static final String LYRICS_API_URL = RestTemplateConfig.LYRICS_API_URL;
    private final RestTemplate restTemplate;

    @Autowired
    public LyricsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String fetchLyrics(String artist, String title) {
        String url = LYRICS_API_URL + artist + "/" + title;

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<JsonNode>() {}
        );

        JsonNode responseBody = response.getBody();
        if (responseBody != null && responseBody.has("error")) {
            return "Lyrics not found";
        }

        String lyrics = responseBody.path("lyrics").asText();
        return cleanLyrics(lyrics);
    }

    private String cleanLyrics(String lyrics) {
        // Removes any text before the first \r\n
        if (lyrics.contains("\r\n")) {
            int index = lyrics.indexOf("\r\n");
            lyrics = lyrics.substring(index + 2);
        }
        // Removes french text
        lyrics = lyrics.replace("Paroles de la chanson", "").trim();
        return lyrics;
    }
}
