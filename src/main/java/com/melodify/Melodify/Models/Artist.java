package com.melodify.Melodify.Models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document
public class Artist {
    @Id
    private String id;
    private String name;
    private String imageUrl;
    private String description;
    private String twitterName;
    private String facebookName;
    private String instagramName;
}
