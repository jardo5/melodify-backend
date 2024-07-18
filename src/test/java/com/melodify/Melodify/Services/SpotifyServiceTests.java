package com.melodify.Melodify.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.melodify.Melodify.Models.TopTrack;
import com.melodify.Melodify.Repositories.TopTrackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SpotifyServiceTests {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private TopTrackRepository topTrackRepository;

    @InjectMocks
    private SpotifyService spotifyService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        spotifyService = new SpotifyService(restTemplate, topTrackRepository);
    }

    @Test
    public void testGetTopSongs_FetchesFromSpotifyAndSaves() {
        TopTrack topTrack = new TopTrack();
        topTrack.setId("top_tracks");
        topTrack.setLastUpdated(LocalDateTime.now(ZoneOffset.UTC).minusDays(2));
        topTrack.setTracks(new ArrayList<>());

        when(topTrackRepository.findById("top_tracks")).thenReturn(Optional.of(topTrack));
        when(restTemplate.exchange(eq("https://accounts.spotify.com/api/token"), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(createTokenResponse(), HttpStatus.OK));
        when(restTemplate.exchange(eq("https://api.spotify.com/v1/playlists/37i9dQZEVXbLRQDuF5jeBp/tracks"), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(createSpotifyResponse(), HttpStatus.OK));

        List<TopTrack.Track> topSongs = spotifyService.getTopSongs();

        assertNotNull(topSongs);
        assertEquals(2, topSongs.size());

        verify(topTrackRepository, times(1)).save(any(TopTrack.class));
    }

    @Test
    public void testGetTopSongs_ReturnsCachedData() {
        TopTrack topTrack = new TopTrack();
        topTrack.setId("top_tracks");
        topTrack.setLastUpdated(LocalDateTime.now(ZoneOffset.UTC));
        List<TopTrack.Track> tracks = new ArrayList<>();
        tracks.add(createTrack("Song 1", "Artist 1", "http://image1.jpg"));
        tracks.add(createTrack("Song 2", "Artist 2", "http://image2.jpg"));
        topTrack.setTracks(tracks);

        when(topTrackRepository.findById("top_tracks")).thenReturn(Optional.of(topTrack));

        List<TopTrack.Track> topSongs = spotifyService.getTopSongs();

        assertNotNull(topSongs);
        assertEquals(2, topSongs.size());
        verify(restTemplate, times(0)).exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    private Map<String, Object> createTokenResponse() {
        return Map.of("access_token", "mocked-access-token");
    }

    private Map<String, Object> createSpotifyResponse() {
        return Map.of(
                "items", List.of(
                        Map.of("track", Map.of(
                                "name", "Song 1",
                                "artists", List.of(Map.of("name", "Artist 1")),
                                "album", Map.of("images", List.of(Map.of("url", "http://image1.jpg")))
                        )),
                        Map.of("track", Map.of(
                                "name", "Song 2",
                                "artists", List.of(Map.of("name", "Artist 2")),
                                "album", Map.of("images", List.of(Map.of("url", "http://image2.jpg")))
                        ))
                )
        );
    }

    private TopTrack.Track createTrack(String name, String artist, String image) {
        TopTrack.Track track = new TopTrack.Track();
        track.setName(name);
        track.setArtist(artist);
        track.setImage(image);
        return track;
    }
}
