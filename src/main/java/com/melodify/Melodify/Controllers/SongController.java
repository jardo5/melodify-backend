package com.melodify.Melodify.Controllers;

import com.melodify.Melodify.Services.GeniusService;
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

    //TODO: FOR Carousel 
    @GetMapping("/top-tracks") // Get top tracks from Spotify API via Top 50 Album Playlist
    public List<Map<String, String>> getTopTracks() {
        return spotifyService.getTopTracks();
    }

    @GetMapping("/search") // Search for Albums, Artists, Tracks via Genius API
    public List<Map<String, String>> search(@RequestParam String query) {
        return geniusService.search(query);
    }
}
