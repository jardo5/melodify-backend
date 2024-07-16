package com.melodify.Melodify.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.melodify.Melodify.Config.RestTemplateConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.logging.Logger;

@Service
public class MusixmatchService {

    private static final Logger LOGGER = Logger.getLogger(MusixmatchService.class.getName());

    private static final String MUSIXMATCH_SEARCH_URL = "https://api.musixmatch.com/ws/1.1/track.search";
    private static final String MUSIXMATCH_LYRICS_URL = "https://api.musixmatch.com/ws/1.1/track.lyrics.get";
    private static final String MUSIXMATCH_API_KEY = RestTemplateConfig.MUSIXMATCH_API_KEY;

    private final RestTemplate restTemplate;

    @Autowired
    public MusixmatchService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String fetchLyrics(String title, String artist) {
        String formattedTitle = title.trim();
        String formattedArtist = artist.trim();
        LOGGER.info("Formatted Title: " + formattedTitle);
        LOGGER.info("Formatted Artist: " + formattedArtist);

        String searchUrl = MUSIXMATCH_SEARCH_URL + "?q_track=" + formattedTitle + "&q_artist=" + formattedArtist + "&apikey=" + MUSIXMATCH_API_KEY;

        LOGGER.info("Search URL: " + searchUrl);

        ResponseEntity<JsonNode> searchResponse = restTemplate.exchange(
                searchUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<JsonNode>() {}
        );

        LOGGER.info("Search Response: " + searchResponse.getBody().toString());

        JsonNode trackList = searchResponse.getBody().path("message").path("body").path("track_list");
        if (trackList.isEmpty()) {
            LOGGER.warning("No tracks found for the given title and artist.");
            return "Lyrics not found";
        }

        String trackId = null;
        for (JsonNode trackNode : trackList) {
            JsonNode track = trackNode.path("track");
            String trackName = track.path("track_name").asText();
            String trackArtist = track.path("artist_name").asText();

            LOGGER.info("Checking track: " + trackName + " by " + trackArtist);

            if (trackName.toLowerCase().contains(formattedTitle.toLowerCase()) && trackArtist.equalsIgnoreCase(formattedArtist)) {
                trackId = track.path("track_id").asText();
                break;
            }
        }

        if (trackId == null) {
            LOGGER.warning("No match found for the given title and artist.");
            return "Lyrics not found";
        }

        String lyricsUrl = MUSIXMATCH_LYRICS_URL + "?track_id=" + trackId + "&apikey=" + MUSIXMATCH_API_KEY;

        ResponseEntity<JsonNode> lyricsResponse = restTemplate.exchange(
                lyricsUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<JsonNode>() {}
        );

        JsonNode lyricsData = lyricsResponse.getBody().path("message").path("body").path("lyrics");
        return lyricsData.path("lyrics_body").asText();
    }
}

//TODO: Completely remove Musixmatch because they cost so fucking much $. No service has a hobby price.
//TODO: Just add a link to the Genius Site for lyrics
