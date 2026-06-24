package me.fatihenes.mywatchlist.media.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JikanAnimeDTO {
    @NotNull(message = "ID cannot be null")
    @JsonProperty("mal_id")
    private Long malId;

    @NotBlank(message = "Title cannot be blank")
    private String title;

    private Double score;

    private String duration;

    private Integer episodes;

    private JikanImages images;

    @Data
    public static class JikanImages {
        public JikanJpg jpg;
    }

    @Data
    public static class JikanJpg {
        @JsonProperty("image_url")
        public String imageUrl;
    }
}
