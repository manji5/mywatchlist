package me.fatihenes.mywatchlist.media.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TmdbDTO {
    @NotNull(message = "ID cannot be null")
    private Long id;

    @NotBlank(message = "Title cannot be blank")
    @JsonAlias({"title", "name"})
    private String title;

    @JsonAlias({"original_title", "original_name"})
    private String originalTitle;

    @JsonProperty("vote_average")
    private Double voteAverage;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonAlias({"release_date", "first_air_date"})
    private String releaseDate;

    @JsonProperty("runtime")
    private Integer runtime;

    @JsonProperty("episode_run_time")
    private List<Integer> episodeRunTime;

    @JsonProperty("number_of_episodes")
    private Integer episodes;

    public Integer getDuration() {
        if (runtime != null)
            return runtime;
        if (episodeRunTime != null && !episodeRunTime.isEmpty())
            return episodeRunTime.get(0);
        return null;
    }

    public void setPosterPath(String posterPath) {
        if (posterPath != null && !posterPath.isEmpty()) {
            this.posterPath = "https://image.tmdb.org/t/p/w500" + posterPath;
        } else {
            this.posterPath = null;
        }
    }

}
