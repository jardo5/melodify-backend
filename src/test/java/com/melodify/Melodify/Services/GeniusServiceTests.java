package com.melodify.Melodify.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.melodify.Melodify.Models.Album;
import com.melodify.Melodify.Models.Artist;
import com.melodify.Melodify.Models.Song;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class GeniusServiceTests {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GeniusService geniusService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetSongDetails() throws IOException {
        // Read the mock JSON from file
        Resource resource = new ClassPathResource("mockResponses/geniusSongResponse.json");
        String mockResponse = new String(Files.readAllBytes(resource.getFile().toPath()));

        JsonNode mockJson = objectMapper.readTree(mockResponse);

        // Mock the API response
        ResponseEntity<JsonNode> responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getBody()).thenReturn(mockJson);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(new ParameterizedTypeReference<JsonNode>() {})
        )).thenReturn(responseEntity);

        // Call the method under test
        Song song = geniusService.getSongDetails("3035222");

        // Assertions
        assertNotNull(song);
        assertEquals("3035222", song.getId());
        assertEquals("Kendrick Lamar", song.getArtist());
        assertEquals("DNA. by Kendrick Lamar", song.getFullTitle());
        assertEquals("https://images.genius.com/f3f77222e1b615e0a10354ea6282ff22.1000x1000x1.png", song.getImageUrl());
        assertEquals("1223592492", song.getAppleMusicId());
        assertNotNull(song.getDescription());
        assertEquals("Apr. 14, 2017", song.getReleaseDate());
        assertNotNull(song.getPageViews());

        Album album = song.getAlbum();
        assertNotNull(album);
        assertEquals("ID of Album", album.getId());
        assertEquals("Full Title of Album", album.getFullTitle());
        assertEquals("https://link.to.album.cover", album.getCoverUrl());
        assertEquals("Release Date of Album", album.getReleaseDate());

        Artist primaryArtist = song.getPrimaryArtist();
        assertNotNull(primaryArtist);
        assertEquals("ID of Artist", primaryArtist.getId());
        assertEquals("Name of Artist", primaryArtist.getName());
        assertEquals("https://link.to.artist.image", primaryArtist.getImageUrl());

        List<Song.ExternalLink> externalLinks = song.getExternalLinks();
        assertNotNull(externalLinks);
        assertEquals(3, externalLinks.size());
        assertEquals("spotify", externalLinks.get(0).getProvider());
        assertEquals("https://open.spotify.com/track/6HZILIRieu8S0iqY8kIKhj", externalLinks.get(0).getUrl());
        assertEquals("soundcloud", externalLinks.get(1).getProvider());
        assertEquals("https://soundcloud.com/kendrick-lamar-music/dna", externalLinks.get(1).getUrl());
        assertEquals("youtube", externalLinks.get(2).getProvider());
        assertEquals("http://www.youtube.com/watch?v=NLZRYQMLDW4", externalLinks.get(2).getUrl());
    } //TODO: Fix this test case
}
