package com.melodify.Melodify.Services.ConnectedAccountsService.SpotifyService;

import com.melodify.Melodify.Config.EnvironmentConfig;
import com.melodify.Melodify.Models.TopTrack;
import com.melodify.Melodify.Repositories.TopTrackRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TopTrackSpotifyService {

    private static final String CLIENT_ID = EnvironmentConfig.SPOTIFY_CLIENT_ID;
    private static final String CLIENT_SECRET = EnvironmentConfig.SPOTIFY_CLIENT_SECRET;
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final String PLAYLIST_URL = "https://api.spotify.com/v1/playlists/37i9dQZEVXbLRQDuF5jeBp/tracks"; //Top 50 USA Playlist

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TopTrackRepo topTrackRepo;

    @Autowired
    public TopTrackSpotifyService(RestTemplate restTemplate, TopTrackRepo topTrackRepo) {
        this.restTemplate = restTemplate;
        this.topTrackRepo = topTrackRepo;
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

    public List<TopTrack.Track> getTopSongs() {
        TopTrack topTrack = topTrackRepo.findById("top_tracks").orElse(null);
        ZonedDateTime nowUTC = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime todayMidnightUTC = nowUTC.toLocalDate().atStartOfDay(ZoneOffset.UTC);

        if (topTrack == null || topTrack.getLastUpdated().isBefore(todayMidnightUTC.toLocalDateTime())) {
            List<TopTrack.Track> topTracks = fetchTopTracksFromSpotify();
            if (topTrack == null) {
                topTrack = new TopTrack();
                topTrack.setId("top_tracks");
            }
            topTrack.setTracks(topTracks);
            topTrack.setLastUpdated(LocalDateTime.now(ZoneOffset.UTC));
            topTrackRepo.save(topTrack);
        }

        return topTrack.getTracks();
    }

    private List<TopTrack.Track> fetchTopTracksFromSpotify() {
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

        List<TopTrack.Track> topTracks = new ArrayList<>();
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

            TopTrack.Track songDetails = new TopTrack.Track();
            songDetails.setName(track.get("name").toString());
            songDetails.setArtist(artistNames.toString());
            if (!images.isEmpty()) {
                songDetails.setImage(images.get(0).get("url").toString());
            }
            topTracks.add(songDetails);
        }

        return topTracks;
    }
}
