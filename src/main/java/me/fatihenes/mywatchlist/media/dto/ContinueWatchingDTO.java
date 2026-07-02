package me.fatihenes.mywatchlist.media.dto;

import me.fatihenes.mywatchlist.media.entity.MediaType;

public record ContinueWatchingDTO(Long id, String externalId, MediaType type, String title,
        String backdropUrl, String posterUrl, Double score, Integer watchedEpisodes,
        Integer totalEpisodes, Double progress) {
}
