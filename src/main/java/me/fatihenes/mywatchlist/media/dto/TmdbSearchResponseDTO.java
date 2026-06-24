package me.fatihenes.mywatchlist.media.dto;

import java.util.List;
import lombok.Data;

@Data
public class TmdbSearchResponseDTO {
    private List<TmdbDTO> results;

}
