package com.melodify.Melodify.Models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Playlist {
    @Id
    private String id; //id
    private String provider; //provider e.g. Spotify, Apple Music, etc.
    private String name; // name of the playlist
    private int totalSongs; // total number of songs in the playlist
    private String imageUrl; // image url of the playlist
    private String description; // description of the playlist
    private String playlistUrl; // url of the playlist
    private List<Song> songs; // list of songs in the playlist
}
