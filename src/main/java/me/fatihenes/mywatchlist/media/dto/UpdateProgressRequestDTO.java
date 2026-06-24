package me.fatihenes.mywatchlist.media.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateProgressRequestDTO(@NotNull(message = "Action cannot be null") Action action,
        @Min(value = 0, message = "Episode count cannot be nagative") Integer value) {
    public enum Action {
        INCREMENT, DECREMENT, SET
    }

}
