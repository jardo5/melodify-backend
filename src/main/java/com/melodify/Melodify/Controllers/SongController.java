package com.melodify.Melodify.Controllers;

import com.melodify.Melodify.Services.SpotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class SongController {

    @Autowired
    private SpotifyService spotifyService;

    //TODO: FOR Carousel 
    @GetMapping("/top-tracks")
    public List<Map<String, String>> getTopTracks() {
        return spotifyService.getTopTracks();
    }
    
    @GetMapping("/test")
    public String test() {
        return "Hello World!";
    }
}
