package me.fatihenes.mywatchlist.media.dto;

import me.fatihenes.mywatchlist.media.entity.MediaType;
import me.fatihenes.mywatchlist.media.entity.WatchStatus;

public record MediaResponseDTO(Long id, String externalId, String title, String posterUrl,
        Double score, Integer duration, Integer episodes, Integer watchedEpisodes,
        Integer totalWatchedMinutes, MediaType type, WatchStatus status) {

}
