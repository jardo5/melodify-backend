package com.melodify.Melodify.Models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "Top_Tracks")
public class TopTrack {

    @Id
    private String id;
    private List<Track> tracks;
    private LocalDateTime lastUpdated;

    @Data
    public static class Track {
        private String name;
        private String artist;
        private String image;
    }
}
