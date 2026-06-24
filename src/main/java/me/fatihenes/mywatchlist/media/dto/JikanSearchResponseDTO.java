package me.fatihenes.mywatchlist.media.dto;

import java.util.List;
import lombok.Data;

@Data
public class JikanSearchResponseDTO {
    private List<JikanAnimeDTO> data;
}
