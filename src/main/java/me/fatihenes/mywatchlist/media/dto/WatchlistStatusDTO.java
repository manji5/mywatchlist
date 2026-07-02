package me.fatihenes.mywatchlist.media.dto;

import me.fatihenes.mywatchlist.media.entity.WatchStatus;

public record WatchlistStatusDTO(boolean saved, WatchStatus status, Integer watchedEpisodes) {

    public static WatchlistStatusDTO notSaved() {
        return new WatchlistStatusDTO(false, null, null);
    }
}
