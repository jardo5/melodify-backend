package com.melodify.Melodify.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class SpotifyPlaylistsResponseDTO {
    @JsonProperty("items")
    private List<SpotifyPlaylistItem> items;

    @Data
    public static class SpotifyPlaylistItem {
        private String id;
        private String name;
        private String description;

        @JsonProperty("external_urls")
        private ExternalUrls externalUrls;

        @JsonProperty("images")
        private List<Image> images;

        @JsonProperty("tracks")
        private Tracks tracks;

        @Data
        public static class ExternalUrls {
            private String spotify;
        }

        @Data
        public static class Image {
            private String url;
        }

        @Data
        public static class Tracks {
            private int total;
        }
    }
}
