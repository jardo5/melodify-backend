package com.melodify.Melodify.Controllers;

import com.melodify.Melodify.Models.Song;
import com.melodify.Melodify.Models.TopTrack;
import com.melodify.Melodify.Models.TopTrack.Track;
import com.melodify.Melodify.Services.ConnectedAccountsService.SpotifyService.SpotifyApiService;
import com.melodify.Melodify.Services.SongService.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/songs")
public class SongController {

    @Autowired
    private  SpotifyApiService spotifyApiService;


    @Autowired
    private SongService songService;

    // Get top tracks from Spotify API via Top 50 Album Playlist
    @GetMapping("/top")
    public ResponseEntity<List<TopTrack.Track>> getTopTracks() {
        List<TopTrack.Track> topTracks = spotifyApiService.getTopSongs();
        return ResponseEntity.ok(topTracks);
    }

    // Search for Albums, Artists, Tracks via Genius API
    @GetMapping("/search")
    public List<Map<String, String>> search(@RequestParam String query) {
        return songService.searchGenius(query);
    }

    // Pulls all metadata, lyrics, and sentiment analysis
    @GetMapping("/{id}")
    public Song getSongDetails(@PathVariable String id) {
        return songService.getSongDetails(id);
    }

    // Only pulls lyrics
    @GetMapping("/lyrics")
    public String getLyrics(@RequestParam String artist, @RequestParam String title) {
        return songService.fetchLyrics(artist, title);
    }

    // Only analyzes sentiment
    @GetMapping("/sentiment")
    public String analyzeSentiment(@RequestParam String lyrics) {
        return songService.analyzeSentiment(lyrics);
    }
}
