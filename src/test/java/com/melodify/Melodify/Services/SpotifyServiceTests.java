package com.melodify.Melodify.Services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class SpotifyServiceTests {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SpotifyService spotifyService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetTopTracks() {
        // Mock the access token response
        String accessToken = "mockedAccessToken";
        ResponseEntity<Map<String, Object>> tokenResponse = mock(ResponseEntity.class);
        when(tokenResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(tokenResponse.getBody()).thenReturn(Map.of("access_token", accessToken));

        when(restTemplate.exchange(
                eq("https://accounts.spotify.com/api/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(new ParameterizedTypeReference<Map<String, Object>>() {})
        )).thenReturn(tokenResponse);

        // Mock the playlist tracks response
        Map<String, Object> mockTrack = Map.of(
                "name", "Mock Track",
                "artists", List.of(Map.of("name", "Mock Artist")),
                "album", Map.of(
                        "images", List.of(
                                Map.of("url", "http://example.com/mock_image.jpg")
                        )
                )
        );

        ResponseEntity<Map<String, Object>> tracksResponse = mock(ResponseEntity.class);
        when(tracksResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(tracksResponse.getBody()).thenReturn(Map.of("items", List.of(Map.of("track", mockTrack))));

        when(restTemplate.exchange(
                eq("https://api.spotify.com/v1/playlists/37i9dQZEVXbLRQDuF5jeBp/tracks"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(new ParameterizedTypeReference<Map<String, Object>>() {})
        )).thenReturn(tracksResponse);

        // Call the method under test
        List<Map<String, String>> topTracks = spotifyService.getTopTracks();

        // Assertions
        assertNotNull(topTracks);
        assertEquals(1, topTracks.size());
        Map<String, String> track = topTracks.get(0);
        assertEquals("Mock Track", track.get("name"));
        assertEquals("Mock Artist", track.get("artist"));
        assertEquals("http://example.com/mock_image.jpg", track.get("image"));
    }
}