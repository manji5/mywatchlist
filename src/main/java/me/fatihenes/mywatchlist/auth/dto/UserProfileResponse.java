package me.fatihenes.mywatchlist.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserProfileResponse(String username, String email,
        @JsonProperty("avatarUrl") String profileImageUrl, UserStatsDTO stats) {
}
