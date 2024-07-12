package com.melodify.Melodify.Models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Song {
    @Id
    private String id;
    private String title;
    private String artist;
    private String fullTitle;
    private String geniusUrl;
    private String imageUrl;
    private String thumbnailUrl;
    private String lyricsUrl;
    private String spotifyUrl;
    private String appleMusicUrl;
    private String description;
}

