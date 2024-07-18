package com.melodify.Melodify.Controllers;

import com.melodify.Melodify.Models.Song;
import com.melodify.Melodify.Services.GeniusService;
import com.melodify.Melodify.Services.LyricsService;
import com.melodify.Melodify.Services.SentimentAnalysisService;
import com.melodify.Melodify.Services.SpotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class SongController {

    @Autowired
    private SpotifyService spotifyService;
    
    @Autowired
    private GeniusService geniusService;
    
    @Autowired
    private LyricsService lyricsService;

    @Autowired
    private SentimentAnalysisService sentimentAnalysisService;
    


    //TODO: FOR Carousel 
    @GetMapping("/top-tracks") // Get top tracks from Spotify API via Top 50 Album Playlist
    public List<Map<String, String>> getTopTracks() {
        return spotifyService.getTopTracks();
    }

    @GetMapping("/search") // Search for Albums, Artists, Tracks via Genius API
    public List<Map<String, String>> search(@RequestParam String query) {
        return geniusService.search(query);
    }

    @GetMapping("/song") // Pulls all metadata, including lyrics
    public Song getSongDetails(@RequestParam String id) {
        return geniusService.getSongDetails(id);
    }

    @GetMapping("/lyrics") // Only pulls lyrics
    public String getLyrics(@RequestParam String artist, @RequestParam String title) {
        return lyricsService.fetchLyrics(artist, title);
    }

    @GetMapping("/analyze-sentiment")
    public String analyzeSentiment(@RequestParam String lyrics) {
        return sentimentAnalysisService.analyzeSentiment(lyrics);
    }
}
