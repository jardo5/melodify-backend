package com.melodify.Melodify.Services.ArtistService;

import com.melodify.Melodify.DTOs.ArtistWithSongsDTO;
import com.melodify.Melodify.DTOs.SongDTO;
import com.melodify.Melodify.Models.Artist;
import com.melodify.Melodify.Repositories.ArtistRepo;
import com.melodify.Melodify.Services.GeniusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArtistService {

    private final GeniusService geniusService;
    private final ArtistRepo artistRepo;

    @Autowired
    public ArtistService(GeniusService geniusService, ArtistRepo artistRepo) {
        this.geniusService = geniusService;
        this.artistRepo = artistRepo;
    }

    public ArtistWithSongsDTO getArtistWithTopSongs(String artistId, int limit) {
        // Check if the artist is in the database
        Artist artist = artistRepo.findById(artistId).orElse(null);
        if (artist != null && artist.getTopSongs() != null && !artist.getTopSongs().isEmpty()) {
            // If artist and top songs are found in the database, return them
            return new ArtistWithSongsDTO(artist, artist.getTopSongs());
        }

        // Fetch artist details from Genius API
        artist = geniusService.getArtistDetails(artistId);
        List<SongDTO> topSongs = geniusService.getArtistTopSongs(artistId, limit);
        artist.setTopSongs(topSongs);

        // Save the fetched artist with top songs in the database
        artistRepo.save(artist);

        return new ArtistWithSongsDTO(artist, topSongs);
    }
}
