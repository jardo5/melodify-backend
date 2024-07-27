package com.melodify.Melodify.Controllers;

import com.melodify.Melodify.DTOs.ArtistWithSongsDTO;
import com.melodify.Melodify.Services.ArtistService.ArtistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/artists")
public class ArtistController {
    
    private final ArtistService artistService;

    @Autowired
    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    @GetMapping("/{artistId}")
    public ArtistWithSongsDTO getArtistWithTopSongs(
            @PathVariable String artistId,
            @RequestParam(defaultValue = "10") int limit) {
        return artistService.getArtistWithTopSongs(artistId, limit);
    }
}
