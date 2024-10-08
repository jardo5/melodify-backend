package com.melodify.Melodify.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.melodify.Melodify.Config.EnvironmentConfig;
import com.melodify.Melodify.DTOs.SongDTO;
import com.melodify.Melodify.Models.Album;
import com.melodify.Melodify.Models.Artist;
import com.melodify.Melodify.Models.Song;
import com.melodify.Melodify.Repositories.SongRepo;
import com.melodify.Melodify.Utils.RateLimiter;
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
import java.util.concurrent.TimeUnit;

@Service
public class GeniusService {

    private static final String GENIUS_API_KEY = EnvironmentConfig.GENIUS_API_KEY;
    private static final String GENIUS_SEARCH_URL = "https://api.genius.com/search";
    private static final String GENIUS_SONGS_URL = "https://api.genius.com/songs/";
    private static final String GENIUS_ARTIST_URL = "https://api.genius.com/artists/";
    private static final String GENIUS_ARTIST_SONGS_URL = GENIUS_ARTIST_URL + "{artistId}/songs";

    private final RestTemplate restTemplate;
    private final RateLimiter rateLimiter;
    
    private final SongRepo songRepo;

    @Autowired
    public GeniusService(RestTemplate restTemplate, RateLimiter rateLimiter, SongRepo songRepo) {
        this.restTemplate = restTemplate;
        this.rateLimiter = rateLimiter;
        this.songRepo = songRepo;
    }
    

    // Search for Tracks via Genius API for Search Bar
    public List<Map<String, String>> search(String query) {
        String url = GENIUS_SEARCH_URL + "?q=" + query;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + GENIUS_API_KEY);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
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
            item.put("id", result.path("id").asText());
            results.add(item);
        }

        return results;
    }

    // Fetch detailed information of a song by ID (Contains Artist, Album, External Links)
    public Song getSongDetails(String songId) {
        String url = GENIUS_SONGS_URL + songId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + GENIUS_API_KEY);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );

        JsonNode songData = response.getBody().path("response").path("song");

        Song song = new Song();
        song.setId(songData.path("id").asText());
        song.setArtist(cleanString(songData.path("artist_names").asText()));
        song.setTitle(cleanString(songData.path("title").asText()));
        song.setFullTitle(cleanString(songData.path("full_title").asText()));
        song.setImageUrl(songData.path("song_art_image_url").asText());
        song.setAppleMusicId(songData.path("apple_music_id").asText());
        song.setDescription(parseDescription(songData.path("description").path("dom").path("children")));
        song.setReleaseDate(songData.path("release_date_with_abbreviated_month_for_display").asText());
        song.setPageViews(songData.path("stats").path("pageviews").asInt());
        song.setGeniusUrl(songData.path("url").asText());

        // Populate album details
        JsonNode albumNode = songData.path("album");
        if (!albumNode.isMissingNode()) {
            Album album = new Album();
            album.setId(albumNode.path("id").asText());
            album.setFullTitle(cleanString(albumNode.path("full_title").asText()));
            album.setCoverUrl(albumNode.path("cover_art_url").asText());
            album.setReleaseDate(albumNode.path("release_date").asText());
            song.setAlbum(album);
        }

        // Populate primary artist details
        JsonNode primaryArtistNode = songData.path("primary_artist");
        if (!primaryArtistNode.isMissingNode()) {
            Artist primaryArtist = new Artist();
            primaryArtist.setId(primaryArtistNode.path("id").asText());
            primaryArtist.setName(cleanString(primaryArtistNode.path("name").asText()));
            primaryArtist.setImageUrl(primaryArtistNode.path("image_url").asText());
            song.setPrimaryArtist(primaryArtist);
        }

        // Populate external links
        List<Song.ExternalLink> externalLinks = new ArrayList<>();
        JsonNode mediaNodes = songData.path("media");
        for (JsonNode mediaNode : mediaNodes) {
            Song.ExternalLink externalLink = new Song.ExternalLink();
            externalLink.setProvider(mediaNode.path("provider").asText());
            externalLink.setUrl(mediaNode.path("url").asText());
            externalLinks.add(externalLink);
        }
        song.setExternalLinks(externalLinks);

        return song;
    }

    // Recursive method to parse description text to aid getSongDetails method
    private String parseDescription(JsonNode children) {
        StringBuilder description = new StringBuilder();

        for (JsonNode child : children) {
            if (child.has("children")) {
                description.append(parseDescription(child.path("children")));
            } else if (child.isTextual()) {
                description.append(cleanString(child.asText()));
            }
        }

        return description.toString();
    }

    // Helper to fix non-breaking space and zero-width space characters
    private String cleanString(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\u200B", "") // Remove ZWSP
                .replace("\u00A0", " ") // Replace NBSP with standard space
                .trim(); // Trim leading/trailing whitespace
    }

    // Fetch detailed information of an artist by ID
    public Artist getArtistDetails(String artistId) {
        String url = GENIUS_ARTIST_URL + artistId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + GENIUS_API_KEY);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        JsonNode artistData = response.getBody().path("response").path("artist");

        Artist artist = new Artist();
        artist.setId(artistData.path("id").asText());
        artist.setName(cleanString(artistData.path("name").asText()));
        artist.setImageUrl(artistData.path("image_url").asText());
        artist.setDescription(parseDescription(artistData.path("description").path("dom").path("children")));
        artist.setTwitterName(cleanString(artistData.path("twitter_name").asText()));
        artist.setFacebookName(cleanString(artistData.path("facebook_name").asText()));
        artist.setInstagramName(cleanString(artistData.path("instagram_name").asText()));

        return artist;
    }

    // Fetch top songs of an artist with a limit (sorted by popularity)
    public List<SongDTO> getArtistTopSongs(String artistId, int limit) {
        String url = GENIUS_ARTIST_SONGS_URL.replace("{artistId}", artistId) + "?sort=popularity&per_page=" + limit;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + GENIUS_API_KEY);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        List<SongDTO> topSongs = new ArrayList<>();
        JsonNode songs = response.getBody().path("response").path("songs");

        for (JsonNode song : songs) {
            SongDTO songDTO = new SongDTO();
            songDTO.setId(song.path("id").asText());
            songDTO.setFullTitle(cleanString(song.path("full_title").asText()));
            songDTO.setImageUrl(song.path("song_art_image_thumbnail_url").asText());
            songDTO.setReleaseDate(song.path("release_date_with_abbreviated_month_for_display").asText());
            topSongs.add(songDTO);
        }

        return topSongs;
    }

    public List<Song> fetchAndPersistSongs(List<String> titles, List<String> artists) {
        List<Song> songs = new ArrayList<>();

        for (int i = 0; i < titles.size(); i++) {
            if (!rateLimiter.tryAcquire()) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            String title = titles.get(i);
            String artist = artists.get(i);

            List<Map<String, String>> searchResults = search(title + " " + artist);
            for (Map<String, String> result : searchResults) {
                if (result.get("name").equalsIgnoreCase(title) && result.get("artist").equalsIgnoreCase(artist)) {
                    Song song = getSongDetails(result.get("id"));
                    songs.add(song);
                    break;
                }
            }
        }

        songRepo.saveAll(songs);
        return songs;
    }
}
