package com.melodify.Melodify.Models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Artist {
    @Id
    private String id; //id
    private String name; //name
    private String imageUrl; //image_url
}
