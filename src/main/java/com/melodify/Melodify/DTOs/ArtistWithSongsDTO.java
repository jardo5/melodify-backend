package com.melodify.Melodify.DTOs;

import com.melodify.Melodify.Models.Artist;
import lombok.Data;
import java.util.List;

@Data
public class ArtistWithSongsDTO {
    private Artist artist;
    private List<SongDTO> topSongs;

    public ArtistWithSongsDTO(Artist artist, List<SongDTO> topSongs) {
        this.artist = artist;
        this.topSongs = topSongs;
    }
}