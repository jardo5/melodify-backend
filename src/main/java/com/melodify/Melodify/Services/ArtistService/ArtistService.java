package com.melodify.Melodify.Services.ArtistService;

import com.melodify.Melodify.DTOs.ArtistWithSongsDTO;
import com.melodify.Melodify.DTOs.SongDTO;
import com.melodify.Melodify.Models.Artist;
import com.melodify.Melodify.Services.GeniusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArtistService {

    private final GeniusService geniusService;

    @Autowired
    public ArtistService(GeniusService geniusService) {
        this.geniusService = geniusService;
    }

    public ArtistWithSongsDTO getArtistWithTopSongs(String artistId, int limit) {
        Artist artist = geniusService.getArtistDetails(artistId);
        List<SongDTO> topSongs = geniusService.getArtistTopSongs(artistId, limit);

        ArtistWithSongsDTO response = new ArtistWithSongsDTO();
        response.setArtist(artist);
        response.setTopSongs(topSongs);

        return response;
    }

    
}
