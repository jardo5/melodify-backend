package com.melodify.Melodify.Models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Album {
    @Id
    private String id; //id
    private String fullTitle; //full_title
    private String coverUrl; //cover_art_url
    private String releaseDate; //release_date
}

