package com.melodify.Melodify.Models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document
public class Playlist {
    @Id
    private String id;
    private String userId; // Reference to the User model
    private String provider;
    private String name;
    private List<Song> songs; // Reference to the Song model
}
