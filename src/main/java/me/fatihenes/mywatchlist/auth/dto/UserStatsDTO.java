package me.fatihenes.mywatchlist.auth.dto;

public record UserStatsDTO(long total, long movies, long series, long anime, long completed,
        long watching, long planned, long onHold, long dropped) {
}
