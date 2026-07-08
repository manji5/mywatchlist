package me.fatihenes.mywatchlist.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PublicUserProfileResponse(String username,
        @JsonProperty("avatarUrl") String profileImageUrl, UserStatsDTO stats) {
}
