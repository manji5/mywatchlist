package me.fatihenes.mywatchlist.auth.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateAvatarRequest(@NotNull(message = "Avatar id cannot be null") @Min(value = 1,
        message = "Avatar id must be at least 1") @Max(value = 20,
                message = "Avatar id must be at most 20") Integer avatarId) {
}
