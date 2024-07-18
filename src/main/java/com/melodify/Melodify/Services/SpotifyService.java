package com.melodify.Melodify.Services;

import com.melodify.Melodify.Config.RestTemplateConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;

@Service
public class SpotifyService {

    private static final String CLIENT_ID = RestTemplateConfig.SPOTIFY_CLIENT_ID;
    private static final String CLIENT_SECRET = RestTemplateConfig.SPOTIFY_CLIENT_SECRET;
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final String PLAYLIST_URL = "https://api.spotify.com/v1/playlists/37i9dQZEVXbLRQDuF5jeBp/tracks"; //Top 50 USA Playlist...They not like us

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public SpotifyService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(CLIENT_ID, CLIENT_SECRET);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                TOKEN_URL,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to fetch access token: " + response.getStatusCode());
        }

        return response.getBody().get("access_token").toString();
    }

    public List<Map<String, String>> getTopSongs() {
        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                PLAYLIST_URL,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to fetch top tracks: " + response.getStatusCode());
        }

        List<Map<String, String>> topTracks = new ArrayList<>();
        List<Map<String, Object>> items = objectMapper.convertValue(response.getBody().get("items"), new TypeReference<>() {
        });

        for (int i = 0; i < 50 && i < items.size(); i++) {
            Map<String, Object> item = items.get(i);
            Map<String, Object> track = objectMapper.convertValue(item.get("track"), new TypeReference<>() {
            });
            Map<String, Object> album = objectMapper.convertValue(track.get("album"), new TypeReference<>() {
            });
            List<Map<String, Object>> images = objectMapper.convertValue(album.get("images"), new TypeReference<>() {
            });

            List<Map<String, Object>> artistList = objectMapper.convertValue(track.get("artists"), new TypeReference<>() {
            });
            StringBuilder artistNames = new StringBuilder();
            for (Map<String, Object> artist : artistList) {
                if (!artistNames.isEmpty()) {
                    artistNames.append(", ");
                }
                artistNames.append(artist.get("name").toString());
            }

            Map<String, String> songDetails = new LinkedHashMap<>();
            songDetails.put("name", track.get("name").toString());
            songDetails.put("artist", artistNames.toString());
            if (!images.isEmpty()) {
                songDetails.put("image", images.get(0).get("url").toString());
            }
            topTracks.add(songDetails);
        }

        return topTracks;
    }
}



