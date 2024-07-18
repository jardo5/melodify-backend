package com.melodify.Melodify.Controllers;

import com.melodify.Melodify.Models.Song;
import com.melodify.Melodify.Services.*;
import com.melodify.Melodify.Services.SongService.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class SongController {

    @Autowired
    private SpotifyService spotifyService;

    @Autowired
    private SongService songService;

    //TODO: FOR Carousel 
    @GetMapping("/top-songs") // Get top tracks from Spotify API via Top 50 Album Playlist
    public List<Map<String, String>> getTopSongs() {
        return spotifyService.getTopSongs();
    }

    @GetMapping("/search") // Search for Albums, Artists, Tracks via Genius API
    public List<Map<String, String>> search(@RequestParam String query) {
        return songService.searchGenius(query);
    }

    @GetMapping("/song") // Pulls all metadata, lyrics, and sentiment analysis
    public Song getSongDetails(@RequestParam String id) {
        return songService.getSongDetails(id);
    }

    @GetMapping("/lyrics") // Only pulls lyrics
    public String getLyrics(@RequestParam String artist, @RequestParam String title) {
        return songService.fetchLyrics(artist, title);
    }

    @GetMapping("/analyze-sentiment") // Only analyzes sentiment
    public String analyzeSentiment(@RequestParam String lyrics) {
        return songService.analyzeSentiment(lyrics);
    }
}
