package me.fatihenes.mywatchlist.media.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class JikanAnimeDetailDTO {

    @JsonProperty("mal_id")
    private Long malId;

    private String title;

    @JsonProperty("title_english")
    private String titleEnglish;

    private String synopsis;

    private Double score;

    private Integer episodes;

    private String duration;

    private String rating;

    private Images images;

    @Data
    public static class Images {

        private Jpg jpg;

    }

    @Data
    public static class Jpg {

        @JsonProperty("image_url")
        private String imageUrl;

        @JsonProperty("large_image_url")
        private String largeImageUrl;

    }

}
