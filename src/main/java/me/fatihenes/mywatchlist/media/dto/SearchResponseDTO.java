package me.fatihenes.mywatchlist.media.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchResponseDTO {

    private TmdbSearchResponseDTO movies;

    private TmdbSearchResponseDTO series;

    private JikanSearchResponseDTO anime;

}
