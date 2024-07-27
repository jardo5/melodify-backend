package com.melodify.Melodify.DTOs;

import lombok.Data;

@Data
public class SongDTO {
    private String id;
    private String fullTitle;
    private String thumbnailUrl;
    private String releaseDate;
}
