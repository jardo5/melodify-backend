package com.melodify.Melodify.Models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Recommendation {
    @Id
    private String id;
    private String userId; // Reference to the User model
    private String songId; // Reference to the Song model
    private String title;
    private String artist;
    private String album;
    private String imageUrl;
    private boolean liked;
    private boolean disliked;
}