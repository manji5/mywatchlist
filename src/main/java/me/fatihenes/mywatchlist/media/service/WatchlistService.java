package me.fatihenes.mywatchlist.media.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import me.fatihenes.mywatchlist.auth.entity.User;
import me.fatihenes.mywatchlist.auth.repository.UserRepository;
import me.fatihenes.mywatchlist.exception.BadRequestException;
import me.fatihenes.mywatchlist.exception.ResourceConflictException;
import me.fatihenes.mywatchlist.exception.ResourceNotFoundException;
import me.fatihenes.mywatchlist.exception.UnauthorizedAccessException;
import me.fatihenes.mywatchlist.media.client.JikanClient;
import me.fatihenes.mywatchlist.media.client.TmdbClient;
import me.fatihenes.mywatchlist.media.dto.ContinueWatchingDTO;
import me.fatihenes.mywatchlist.media.dto.JikanAnimeDTO;
import me.fatihenes.mywatchlist.media.dto.MediaResponseDTO;
import me.fatihenes.mywatchlist.media.dto.TmdbDTO;
import me.fatihenes.mywatchlist.media.dto.UpdateProgressRequestDTO;
import me.fatihenes.mywatchlist.media.dto.WatchlistStatusDTO;
import me.fatihenes.mywatchlist.media.entity.Media;
import me.fatihenes.mywatchlist.media.entity.MediaType;
import me.fatihenes.mywatchlist.media.entity.WatchStatus;
import me.fatihenes.mywatchlist.media.repository.MediaRepository;

@Service
@RequiredArgsConstructor
public class WatchlistService {
    private final MediaRepository mediaRepository;
    private final UserRepository userRepository;
    private final TmdbClient tmdbClient;
    private final JikanClient jikanClient;

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new ResourceNotFoundException("User not found with username: " + username));
    }

    private Media getMediaAndVerifyOwner(Long mediaId, String username) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found."));

        if (!media.getUser().getUsername().equals(username)) {
            throw new UnauthorizedAccessException(
                    "Unauthorized: You cannot modify someone else's list.");
        }

        return media;
    }

    private Media getMediaByExternalIdAndVerifyOwner(String externalId, MediaType type,
            String username) {
        Media media = mediaRepository
                .findByExternalIdAndTypeAndUserUsername(externalId, type, username).orElseThrow(
                        () -> new ResourceNotFoundException("Media not found in your watchlist."));
        return media;
    }

    public void saveMovie(Long movieId, String username) {
        if (mediaRepository.existsByExternalIdAndTypeAndUserUsername(String.valueOf(movieId),
                MediaType.MOVIE, username)) {
            throw new ResourceConflictException("This movie is already in your list.");
        }

        User user = getUserByUsername(username);

        TmdbDTO dto = tmdbClient.fetchMovieDetails(movieId);

        Media media = Media.builder().externalId(String.valueOf(dto.getId()))
                .title(dto.getTitle() != null ? dto.getTitle() : dto.getOriginalTitle())
                .posterUrl(dto.getPosterPath()).score(dto.getVoteAverage())
                .duration(dto.getDuration()).episodes(1).watchedEpisodes(0).type(MediaType.MOVIE)
                .user(user).build();

        mediaRepository.save(media);
    }

    public void saveSeries(Long showId, String username) {
        if (mediaRepository.existsByExternalIdAndTypeAndUserUsername(String.valueOf(showId),
                MediaType.TV_SERIES, username)) {
            throw new ResourceConflictException("This show is already in your list.");
        }

        User user = getUserByUsername(username);

        TmdbDTO dto = tmdbClient.fetchSeriesDetails(showId);

        Media media = Media.builder().externalId(String.valueOf(dto.getId()))
                .title(dto.getTitle() != null ? dto.getTitle() : dto.getOriginalTitle())
                .posterUrl(dto.getPosterPath()).score(dto.getVoteAverage())
                .type(MediaType.TV_SERIES).user(user).duration(dto.getDuration())
                .episodes(dto.getEpisodes() != null ? dto.getEpisodes() : 0).watchedEpisodes(0)
                .build();

        mediaRepository.save(media);
    }

    private Integer parseJikanDuration(String durationStr) {
        if (durationStr == null || durationStr.isBlank()
                || durationStr.equalsIgnoreCase("Unknown")) {
            return 0;
        }

        int totalMinutes = 0;

        java.util.regex.Matcher hrMatcher =
                java.util.regex.Pattern.compile("(\\d+)\\s*hr").matcher(durationStr);
        if (hrMatcher.find()) {
            totalMinutes += Integer.parseInt(hrMatcher.group(1)) * 60;
        }

        java.util.regex.Matcher minMatcher =
                java.util.regex.Pattern.compile("(\\d+)\\s*min").matcher(durationStr);
        if (minMatcher.find()) {
            totalMinutes += Integer.parseInt(minMatcher.group(1));
        }

        return totalMinutes;
    }

    public void saveAnime(Long malId, String username) {
        if (mediaRepository.existsByExternalIdAndTypeAndUserUsername(String.valueOf(malId),
                MediaType.ANIME, username)) {
            throw new ResourceConflictException("This anime is already in your list.");
        }

        User user = getUserByUsername(username);

        JikanAnimeDTO dto = jikanClient.fetchAnimeDetails(malId);

        String poster = null;
        if (dto.getImages() != null && dto.getImages().getJpg() != null) {
            poster = dto.getImages().getJpg().getImageUrl();
        }

        Media media = Media.builder().externalId(String.valueOf(dto.getMalId()))
                .title(dto.getTitle()).posterUrl(poster).score(dto.getScore())
                .duration(parseJikanDuration(dto.getDuration()))
                .episodes(dto.getEpisodes() != null ? dto.getEpisodes() : 0).watchedEpisodes(0)
                .type(MediaType.ANIME).user(user).build();

        mediaRepository.save(media);
    }

    // Updates the number of watched episodes (increase / decrease / direct set).

    public void updateWatchedEpisodes(Long mediaId, UpdateProgressRequestDTO request,
            String username) {
        Media media = getMediaAndVerifyOwner(mediaId, username);

        int totalEpisodes = media.getEpisodes() != null ? media.getEpisodes() : 0;
        int current = media.getWatchedEpisodes() != null ? media.getWatchedEpisodes() : 0;
        int updated;

        switch (request.action()) {
            case INCREMENT -> updated = current + 1;
            case DECREMENT -> updated = current - 1;
            case SET -> {
                if (request.value() == null) {
                    throw new BadRequestException("value field is required for SET action.");
                }
                updated = request.value();
            }

            default -> throw new BadRequestException("Unknown action: " + request.action());
        }

        if (updated < 0) {
            throw new BadRequestException("Watched episodes cannot be negative.");
        }
        if (totalEpisodes > 0 && updated > totalEpisodes) {
            throw new BadRequestException(
                    "Watched episodes cannot exceed total episodes (" + totalEpisodes + ").");
        }

        media.setWatchedEpisodes(updated);

        if (totalEpisodes > 0 && updated == totalEpisodes) {
            media.setStatus(WatchStatus.COMPLETED);
        } else if (updated > 0 && media.getStatus() == WatchStatus.PLAN_TO_WATCH) {
            media.setStatus(WatchStatus.WATCHING);
        } else if (updated == 0 && media.getStatus() == WatchStatus.WATCHING) {
            media.setStatus(WatchStatus.PLAN_TO_WATCH);
        }

        mediaRepository.save(media);
    }

    public void updateMovieWatchedEpisodes(Long movieId, UpdateProgressRequestDTO request,
            String username) {
        Media media = getMediaByExternalIdAndVerifyOwner(String.valueOf(movieId), MediaType.MOVIE,
                username);

        int totalEpisodes = media.getEpisodes() != null ? media.getEpisodes() : 0;
        int current = media.getWatchedEpisodes() != null ? media.getWatchedEpisodes() : 0;
        int updated;

        switch (request.action()) {
            case INCREMENT -> updated = current + 1;
            case DECREMENT -> updated = current - 1;
            case SET -> {
                if (request.value() == null) {
                    throw new BadRequestException("value field is required for SET action.");
                }
                updated = request.value();
            }

            default -> throw new BadRequestException("Unknown action: " + request.action());
        }

        if (updated < 0) {
            throw new BadRequestException("Watched episodes cannot be negative.");
        }
        if (totalEpisodes > 0 && updated > totalEpisodes) {
            throw new BadRequestException(
                    "Watched episodes cannot exceed total episodes (" + totalEpisodes + ").");
        }

        media.setWatchedEpisodes(updated);

        if (totalEpisodes > 0 && updated == totalEpisodes) {
            media.setStatus(WatchStatus.COMPLETED);
        } else if (updated > 0 && media.getStatus() == WatchStatus.PLAN_TO_WATCH) {
            media.setStatus(WatchStatus.WATCHING);
        } else if (updated == 0 && media.getStatus() == WatchStatus.WATCHING) {
            media.setStatus(WatchStatus.PLAN_TO_WATCH);
        }

        mediaRepository.save(media);
    }

    public void updateSeriesWatchedEpisodes(Long seriesId, UpdateProgressRequestDTO request,
            String username) {
        Media media = getMediaByExternalIdAndVerifyOwner(String.valueOf(seriesId),
                MediaType.TV_SERIES, username);

        int totalEpisodes = media.getEpisodes() != null ? media.getEpisodes() : 0;
        int current = media.getWatchedEpisodes() != null ? media.getWatchedEpisodes() : 0;
        int updated;

        switch (request.action()) {
            case INCREMENT -> updated = current + 1;
            case DECREMENT -> updated = current - 1;
            case SET -> {
                if (request.value() == null) {
                    throw new BadRequestException("value field is required for SET action.");
                }
                updated = request.value();
            }

            default -> throw new BadRequestException("Unknown action: " + request.action());
        }

        if (updated < 0) {
            throw new BadRequestException("Watched episodes cannot be negative.");
        }
        if (totalEpisodes > 0 && updated > totalEpisodes) {
            throw new BadRequestException(
                    "Watched episodes cannot exceed total episodes (" + totalEpisodes + ").");
        }

        media.setWatchedEpisodes(updated);

        if (totalEpisodes > 0 && updated == totalEpisodes) {
            media.setStatus(WatchStatus.COMPLETED);
        } else if (updated > 0 && media.getStatus() == WatchStatus.PLAN_TO_WATCH) {
            media.setStatus(WatchStatus.WATCHING);
        } else if (updated == 0 && media.getStatus() == WatchStatus.WATCHING) {
            media.setStatus(WatchStatus.PLAN_TO_WATCH);
        }

        mediaRepository.save(media);
    }

    public void updateAnimeWatchedEpisodes(Long malId, UpdateProgressRequestDTO request,
            String username) {
        Media media = getMediaByExternalIdAndVerifyOwner(String.valueOf(malId), MediaType.ANIME,
                username);

        int totalEpisodes = media.getEpisodes() != null ? media.getEpisodes() : 0;
        int current = media.getWatchedEpisodes() != null ? media.getWatchedEpisodes() : 0;
        int updated;

        switch (request.action()) {
            case INCREMENT -> updated = current + 1;
            case DECREMENT -> updated = current - 1;
            case SET -> {
                if (request.value() == null) {
                    throw new BadRequestException("value field is required for SET action.");
                }
                updated = request.value();
            }

            default -> throw new BadRequestException("Unknown action: " + request.action());
        }

        if (updated < 0) {
            throw new BadRequestException("Watched episodes cannot be negative.");
        }
        if (totalEpisodes > 0 && updated > totalEpisodes) {
            throw new BadRequestException(
                    "Watched episodes cannot exceed total episodes (" + totalEpisodes + ").");
        }

        media.setWatchedEpisodes(updated);

        if (totalEpisodes > 0 && updated == totalEpisodes) {
            media.setStatus(WatchStatus.COMPLETED);
        } else if (updated > 0 && media.getStatus() == WatchStatus.PLAN_TO_WATCH) {
            media.setStatus(WatchStatus.WATCHING);
        } else if (updated == 0 && media.getStatus() == WatchStatus.WATCHING) {
            media.setStatus(WatchStatus.PLAN_TO_WATCH);
        }

        mediaRepository.save(media);
    }

    // Updates watch status manually.
    // If you want to make COMPLETED, at least 1 section must be defined.
    public void updateWatchStatus(Long mediaId, WatchStatus status, String username) {
        Media media = getMediaAndVerifyOwner(mediaId, username);

        if (status == WatchStatus.COMPLETED && media.getEpisodes() == 0) {
            throw new BadRequestException(
                    "You cannot mark a series as COMPLETED if no episodes are watched.");
        }
        // While COMPLETED, pull watchedEpisodes to complete.
        if (status == WatchStatus.COMPLETED) {
            media.setWatchedEpisodes(media.getEpisodes());
        }

        media.setStatus(status);
        mediaRepository.save(media);
    }

    public void updateMovieStatus(Long movieId, WatchStatus status, String username) {
        Media media = getMediaByExternalIdAndVerifyOwner(String.valueOf(movieId), MediaType.MOVIE,
                username);

        if (status == WatchStatus.COMPLETED && media.getEpisodes() == 0) {
            throw new BadRequestException(
                    "You cannot mark a series as COMPLETED if no episodes are defined.");
        }

        if (status == WatchStatus.COMPLETED) {
            media.setWatchedEpisodes(media.getEpisodes());
        }

        media.setStatus(status);
        mediaRepository.save(media);
    }


    public void updateSeriesStatus(Long showId, WatchStatus status, String username) {
        Media media = getMediaByExternalIdAndVerifyOwner(String.valueOf(showId),
                MediaType.TV_SERIES, username);

        if (status == WatchStatus.COMPLETED && media.getEpisodes() == 0) {
            throw new BadRequestException(
                    "You cannot mark a series as COMPLETED if no episodes are defined.");
        }

        if (status == WatchStatus.COMPLETED) {
            media.setWatchedEpisodes(media.getEpisodes());
        }

        media.setStatus(status);
        mediaRepository.save(media);
    }

    public void updateAnimeStatus(Long animeId, WatchStatus status, String username) {
        Media media = getMediaByExternalIdAndVerifyOwner(String.valueOf(animeId), MediaType.ANIME,
                username);

        if (status == WatchStatus.COMPLETED && media.getEpisodes() == 0) {
            throw new BadRequestException(
                    "You cannot mark a series as COMPLETED if no episodes are defined.");
        }

        if (status == WatchStatus.COMPLETED) {
            media.setWatchedEpisodes(media.getEpisodes());
        }

        media.setStatus(status);
        mediaRepository.save(media);
    }


    public void deleteMedia(Long mediaId, String username) {
        Media media = getMediaAndVerifyOwner(mediaId, username);
        mediaRepository.delete(media);
    }

    public WatchlistStatusDTO getWatchlistStatus(Long externalId, MediaType type, String username) {
        return mediaRepository
                .findByExternalIdAndTypeAndUserUsername(String.valueOf(externalId), type, username)
                .map(media -> new WatchlistStatusDTO(true, media.getStatus(),
                        media.getWatchedEpisodes()))
                .orElse(WatchlistStatusDTO.notSaved());
    }

    // Calculates the total viewing time in minutes.
    private Integer calculateTotalWatchedMinutes(Media media) {
        int watched = media.getWatchedEpisodes() != null ? media.getWatchedEpisodes() : 0;
        int duration = media.getDuration() != null ? media.getDuration() : 0;

        return watched * duration;
    }

    // Converts the Media entity to MediaResponseDTO.
    private MediaResponseDTO toDto(Media media) {
        return new MediaResponseDTO(media.getId(), media.getExternalId(), media.getTitle(),
                media.getOriginalTitle(), media.getBackdropUrl(), media.getPosterUrl(),
                media.getScore(), media.getDuration(), media.getEpisodes(),
                media.getWatchedEpisodes(), calculateTotalWatchedMinutes(media), media.getType(),
                media.getStatus());
    }

    // Getting user watchlist
    public Page<MediaResponseDTO> getWatchlistByUsername(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        return mediaRepository.findAllByUserUsername(username, pageable).map(this::toDto);
    }

    private ContinueWatchingDTO toContinueDto(Media media) {
        int total = media.getEpisodes() != null ? media.getEpisodes() : 0;
        int watched = media.getWatchedEpisodes() != null ? media.getWatchedEpisodes() : 0;
        double progress = total > 0 ? (double) watched / total : 0.0;

        return new ContinueWatchingDTO(media.getId(), media.getExternalId(), media.getType(),
                media.getTitle(), media.getBackdropUrl(), media.getPosterUrl(), media.getScore(),
                watched, total, progress);
    }

    public List<ContinueWatchingDTO> getContinueWatching(String username) {
        return mediaRepository.findTop10ByUserUsernameAndStatusOrderByUpdatedAtDesc(username,
                WatchStatus.WATCHING).stream().map(this::toContinueDto).toList();
    }

    public List<ContinueWatchingDTO> getRecentlyAdded(String username) {
        return mediaRepository.findTop10ByUserUsernameOrderByCreatedAtDesc(username).stream()
                .map(this::toContinueDto).toList();
    }
}
