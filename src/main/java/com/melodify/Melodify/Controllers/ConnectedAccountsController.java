package com.melodify.Melodify.Controllers;

import com.melodify.Melodify.Services.ConnectedAccountsService.SpotifyService.SpotifyAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/providers")
public class ConnectedAccountsController {

    @Autowired
    private SpotifyAuthService spotifyAuthService;

    @GetMapping("/spotify/login")
    public Map<String, String> spotifyLogin() {
        String authorizationUrl = spotifyAuthService.getAuthorizationUrl();
        Map<String, String> response = new HashMap<>();
        response.put("authorizationUrl", authorizationUrl);
        return response;
    }

    @GetMapping("/spotify/callback")
    public Map<String, String>spotifyCallback(@RequestParam String code, @RequestParam String state, @RequestParam String token) {
        spotifyAuthService.handleSpotifyCallback(code, state, token);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Successfully connected Spotify account");
        return response;
    }
    
    
}
