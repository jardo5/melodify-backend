package com.melodify.Melodify.Models;

import lombok.Data;

import java.util.List;

@Data
public class GeniusResponse {
    private Meta meta;
    private Response response;

    @Data
    public static class Meta {
        private int status;
    }

    @Data
    public static class Response {
        private List<Hit> hits;
    }

    @Data
    public static class Hit {
        private Result result;
    }

    @Data
    public static class Result {
        private String title;
        private String artist_names;
        private String full_title;
        private String header_image_thumbnail_url;
        private String header_image_url;
        private int id;
        private String lyrics_state;
        private String path;
        private PrimaryArtist primary_artist;
        private String song_art_image_thumbnail_url;
        private String song_art_image_url;
        private Stats stats;
        private String url;

        @Data
        public static class PrimaryArtist {
            private String name;
            private String header_image_url;
            private String image_url;
            private boolean is_meme_verified;
            private boolean is_verified;
            private int id;
            private String url;
        }

        @Data
        public static class Stats {
            private int unreviewed_annotations;
            private boolean hot;
            private int pageviews;
        }
    }
}
