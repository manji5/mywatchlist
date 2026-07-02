package me.fatihenes.mywatchlist.media.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HomeResponseDTO {

    private TmdbSearchResponseDTO trendingMovies;

    private TmdbSearchResponseDTO trendingSeries;

    private JikanSearchResponseDTO trendingAnime;

}
