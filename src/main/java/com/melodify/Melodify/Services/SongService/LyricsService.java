package com.melodify.Melodify.Services.SongService;

import com.fasterxml.jackson.databind.JsonNode;
import com.melodify.Melodify.Config.RestTemplateConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );

            JsonNode responseBody = response.getBody();
            if (responseBody != null && responseBody.has("error")) {
                return "Lyrics not found";
            }

            String lyrics = responseBody.path("lyrics").asText();
            return cleanLyrics(lyrics);
        } catch (RestClientException e) {
            return "Lyrics not found";
        }
    }

    public String fetchLyricsWithTimeout(String artist, String title, long timeout, TimeUnit unit) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> fetchLyrics(artist, title));

        try {
            return future.get(timeout, unit);
        } catch (TimeoutException e) {
            future.cancel(true);
            return "Lyrics request timed out";
        } catch (Exception e) {
            return "Lyrics not found";
        }
    }

    private String cleanLyrics(String lyrics) {
        // Removes any text before the first \r\n
        if (lyrics.contains("\r\n")) {
            int index = lyrics.indexOf("\r\n");
            lyrics = lyrics.substring(index + 2);
        }
        // Removes French text
        lyrics = lyrics.replace("Paroles de la chanson", "").trim();
        return lyrics;
    }
}
