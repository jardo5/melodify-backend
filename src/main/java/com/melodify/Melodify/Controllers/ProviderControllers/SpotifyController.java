package com.melodify.Melodify.Controllers.ProviderControllers;

import com.melodify.Melodify.Models.Playlist;
import com.melodify.Melodify.Models.Song;
import com.melodify.Melodify.Services.ConnectedAccountsService.SpotifyService.SpotifyApiService;
import com.melodify.Melodify.Services.SongCrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/spotify")
public class SpotifyController {

    @Autowired
    private SpotifyApiService spotifyApiService;
    
    @Autowired
    private SongCrawlerService songCrawlService;

    @GetMapping("/playlists")
    public List<Playlist> getUserPlaylists(@RequestHeader("Authorization") String bearerToken) {
        return spotifyApiService.getUserPlaylists(bearerToken);
    }

    
    
    //NOTE: DO NOT ABUSE THIS ENDPOINT. REFER TO SongCrawlerService FOR MORE INFORMATION
    /* 
    @PostMapping("/crawl-and-persist")
    public ResponseEntity<Map<String, Object>> crawlAndPersistSongs(@RequestHeader("Authorization") String bearerToken) {
        Map<String, String> playlistIDs = spotifyApiService.fetchPlaylistIdsFromFeaturedPlaylists(bearerToken);
        songCrawlService.crawlSongsFromSpotify(playlistIDs, bearerToken);
        songCrawlService.convertToGeniusSongsAndPersist();

        return ResponseEntity.ok(new HashMap<>() {{
            put("message", "Successfully crawled and persisted songs from Spotify to Genius");
        }});
    } 
    */
}
