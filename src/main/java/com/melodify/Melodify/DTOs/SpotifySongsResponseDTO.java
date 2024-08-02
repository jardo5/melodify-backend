package com.melodify.Melodify.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class SpotifySongsResponseDTO {
    @JsonProperty("items")
    private List<TrackItem> items;

    @Data
    public static class TrackItem {
        @JsonProperty("track")
        private Track track;

        @Data
        public static class Track {
            private String id;
            private String name;

            @JsonProperty("artists")
            private List<Artist> artists;

            @JsonProperty("album")
            private Album album;

            @JsonProperty("external_urls")
            private ExternalUrls externalUrls;

            @Data
            public static class Artist {
                private String name;
            }

            @Data
            public static class Album {
                @JsonProperty("images")
                private List<Image> images;

                @Data
                public static class Image {
                    private String url;
                }
            }

            @Data
            public static class ExternalUrls {
                private String spotify;
            }
        }
    }
}
