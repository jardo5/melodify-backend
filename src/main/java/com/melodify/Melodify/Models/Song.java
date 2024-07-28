package com.melodify.Melodify.Models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


@Data
@Document(collection = "Song")
public class Song {
    @Id
    private String id; //id
    private String artist; //artist_names
    private String title; //title
    private String fullTitle; //full_title
    private String imageUrl; //song_art_image_url
    private String appleMusicId; //apple_music_id
    private String description; //description.
    private String releaseDate; //release_date_with_abbreviated_month_for_display
    private int pageViews; //stats.pageviews
    private String geniusUrl; //url
    private List<ExternalLink> externalLinks;

    private Album album; //album
    private Artist primaryArtist; //album.artist
    
    // For sentiment analysis
    private String lyrics; //lyrics
    private String sentiment; //sentiment

    @Data
    public static class ExternalLink {
        private String provider; //provider
        private String url; //url
    }
}

