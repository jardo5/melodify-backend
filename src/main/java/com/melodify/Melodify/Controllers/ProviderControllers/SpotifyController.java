package com.melodify.Melodify.Controllers.ProviderControllers;

import com.melodify.Melodify.Models.Playlist;
import com.melodify.Melodify.Services.ConnectedAccountsService.SpotifyService.SpotifyApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/spotify")
public class SpotifyController {

    @Autowired
    private SpotifyApiService spotifyApiService;

    @GetMapping("/playlists")
    public List<Playlist> getUserPlaylists(@RequestHeader("Authorization") String bearerToken) {
        return spotifyApiService.getUserPlaylists(bearerToken);
    }
}
