package com.melodify.Melodify.Models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "Recommendation")
public class Recommendation {

    @Id
    private String id;
    private String userId;
    private List<String> recommendedSongIds;

    public Recommendation(String userId, List<String> recommendedSongIds) {
        this.userId = userId;
        this.recommendedSongIds = recommendedSongIds;
    }
}
