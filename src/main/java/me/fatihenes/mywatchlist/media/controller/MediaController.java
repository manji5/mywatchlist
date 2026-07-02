package me.fatihenes.mywatchlist.media.controller;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.fatihenes.mywatchlist.media.dto.ContinueWatchingDTO;
import me.fatihenes.mywatchlist.media.dto.HomeResponseDTO;
import me.fatihenes.mywatchlist.media.dto.JikanAnimeDetailDTO;
import me.fatihenes.mywatchlist.media.dto.JikanSearchResponseDTO;
import me.fatihenes.mywatchlist.media.dto.MediaResponseDTO;
import me.fatihenes.mywatchlist.media.dto.TmdbSearchResponseDTO;
import me.fatihenes.mywatchlist.media.dto.UpdateProgressRequestDTO;
import me.fatihenes.mywatchlist.media.dto.WatchlistStatusDTO;
import me.fatihenes.mywatchlist.media.entity.MediaType;
import me.fatihenes.mywatchlist.media.entity.WatchStatus;
import me.fatihenes.mywatchlist.media.service.JikanApiService;
import me.fatihenes.mywatchlist.media.service.TmdbApiService;
import me.fatihenes.mywatchlist.media.service.WatchlistService;
import me.fatihenes.mywatchlist.media.dto.SearchResponseDTO;
import me.fatihenes.mywatchlist.media.dto.TmdbDetailDTO;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {
    private final JikanApiService jikanApiService;
    private final TmdbApiService tmdbApiService;
    private final WatchlistService watchlistService;

    // GET http://localhost:8080/api/media/anime?query=
    @GetMapping("/anime")
    public ResponseEntity<JikanSearchResponseDTO> searchAnime(@RequestParam String query) {
        return ResponseEntity.ok(jikanApiService.searchAnime(query));
    }

    // GET http://localhost:8080/api/media/movie?query=
    @GetMapping("/movie")
    public ResponseEntity<TmdbSearchResponseDTO> searchMovie(@RequestParam String query) {
        return ResponseEntity.ok(tmdbApiService.searchMovie(query));
    }

    // GET http://localhost:8080/api/media/series?query=
    @GetMapping("/series")
    public ResponseEntity<TmdbSearchResponseDTO> searchSeries(@RequestParam String query) {
        return ResponseEntity.ok(tmdbApiService.searchSeries(query));
    }

    @GetMapping("/movie/{id}")
    public ResponseEntity<TmdbDetailDTO> getMovie(@PathVariable Long id) {
        return ResponseEntity.ok(tmdbApiService.getMovie(id));
    }

    @GetMapping("/series/{id}")
    public ResponseEntity<TmdbDetailDTO> getSeries(@PathVariable Long id) {
        return ResponseEntity.ok(tmdbApiService.getSeries(id));
    }

    @GetMapping("/anime/{id}")
    public ResponseEntity<JikanAnimeDetailDTO> getAnime(@PathVariable Long id) {
        return ResponseEntity.ok(jikanApiService.getAnime(id));
    }

    // POST http://localhost:8080/api/media/save/anime/{movieId}
    @PostMapping("/save/anime/{malID}")
    public ResponseEntity<String> saveAnimeToWatchlist(@Valid @PathVariable Long malID) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        watchlistService.saveAnime(malID, username);

        return ResponseEntity.ok("Anime successfully added to watchlist.");
    }

    // POST http://localhost:8080/api/media/save/movie/{movieId}
    @PostMapping("/save/movie/{movieId}")
    public ResponseEntity<String> saveMovieToWatchlist(@Valid @PathVariable Long movieId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        watchlistService.saveMovie(movieId, username);

        return ResponseEntity.ok("Movie successfully added to watchlist.");
    }

    // POST http://localhost:8080/api/media/save/series/{seriesId}
    @PostMapping("/save/series/{seriesId}")
    public ResponseEntity<String> saveSeriesToWatchlist(@Valid @PathVariable Long seriesId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        watchlistService.saveSeries(seriesId, username);

        return ResponseEntity.ok("TV Series successfully added to watchlist.");
    }

    // GET http://localhost:8080/api/media/watchlist?page=0&size=10
    @GetMapping("/watchlist")
    public ResponseEntity<Page<MediaResponseDTO>> getUserWatchlist(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(watchlistService.getWatchlistByUsername(username, page, size));
    }

    @GetMapping("/movie/{id}/watchlist")
    public ResponseEntity<WatchlistStatusDTO> getMovieWatchlistStatus(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok(watchlistService.getWatchlistStatus(id, MediaType.MOVIE, username));
    }

    @GetMapping("/series/{id}/watchlist")
    public ResponseEntity<WatchlistStatusDTO> getSeriesWatchlistStatus(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok(watchlistService.getWatchlistStatus(id, MediaType.TV_SERIES, username));
    }

    @GetMapping("/anime/{id}/watchlist")
    public ResponseEntity<WatchlistStatusDTO> getAnimeWatchlistStatus(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok(watchlistService.getWatchlistStatus(id, MediaType.ANIME, username));
    }

    // GET http://localhost:8080/api/media/watchlist/{targetUsername}
    @GetMapping("/watchlist/{targetUsername}")
    public ResponseEntity<Page<MediaResponseDTO>> getOtherWatchlist(
            @PathVariable String targetUsername, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity
                .ok(watchlistService.getWatchlistByUsername(targetUsername, page, size));
    }

    // PATCH http://localhost:8080/api/media/status/{id}?status=COMPLETED
    @PatchMapping("/status/{id}")
    public ResponseEntity<String> updateMediaStatus(@PathVariable Long id,
            @RequestParam WatchStatus status) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        watchlistService.updateWatchStatus(id, status, username);

        return ResponseEntity.ok("Media status successfully updated to " + status.name());
    }

    @PatchMapping("/movie/{id}/status")
    public ResponseEntity<String> updateMovieStatus(@PathVariable Long id,
            @RequestParam WatchStatus status) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        watchlistService.updateMovieStatus(id, status, username);

        return ResponseEntity.ok("Media status successfully updated to " + status.name());
    }

    @PatchMapping("/series/{id}/status")
    public ResponseEntity<String> updateSeriesStatus(@PathVariable Long id,
            @RequestParam WatchStatus status) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        watchlistService.updateSeriesStatus(id, status, username);

        return ResponseEntity.ok("Media status successfully updated to " + status.name());
    }

    @PatchMapping("/anime/{id}/status")
    public ResponseEntity<String> updateAnimeStatus(@PathVariable Long id,
            @RequestParam WatchStatus status) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        watchlistService.updateAnimeStatus(id, status, username);

        return ResponseEntity.ok("Media status successfully updated to " + status.name());
    }

    // PATCH http://localhost:8080/api/media/progress/{id}
    @PatchMapping("/progress/{id}")
    public ResponseEntity<String> updateProgress(@PathVariable Long id,
            @Valid @RequestBody UpdateProgressRequestDTO request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        watchlistService.updateWatchedEpisodes(id, request, username);

        return ResponseEntity.ok("Progress successfully updated.");
    }

    @PatchMapping("/movie/{id}/progress")
    public ResponseEntity<String> updateMovieStatus(@PathVariable Long id,
            @Valid @RequestBody UpdateProgressRequestDTO requestDTO) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        watchlistService.updateMovieWatchedEpisodes(id, requestDTO, username);

        return ResponseEntity.ok("Movie progress successfully updated.");
    }

    @PatchMapping("/series/{id}/progress")
    public ResponseEntity<String> updateSeriesStatus(@PathVariable Long id,
            @Valid @RequestBody UpdateProgressRequestDTO requestDTO) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        watchlistService.updateSeriesWatchedEpisodes(id, requestDTO, username);

        return ResponseEntity.ok("Tv Show progress successfully updated.");
    }

    @PatchMapping("/anime/{id}/progress")
    public ResponseEntity<String> updateAnimeStatus(@PathVariable Long id,
            @Valid @RequestBody UpdateProgressRequestDTO requestDTO) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        watchlistService.updateAnimeWatchedEpisodes(id, requestDTO, username);

        return ResponseEntity.ok("Anime progress successfully updated.");
    }

    // DELETE http://localhost:8080/api/media/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteMediaFromWatchlist(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        watchlistService.deleteMedia(id, username);
        return ResponseEntity.ok("Media successfully removed from your watchlist.");
    }

    @GetMapping("/home")
    public ResponseEntity<HomeResponseDTO> getHomeData() {

        HomeResponseDTO response = new HomeResponseDTO(tmdbApiService.trendingMovies(),
                tmdbApiService.trendingSeries(), jikanApiService.trendingAnime());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/trending/movies")
    public ResponseEntity<TmdbSearchResponseDTO> getTrendingMovie() {

        return ResponseEntity.ok(tmdbApiService.trendingMovies());
    }

    @GetMapping("/trending/series")
    public ResponseEntity<TmdbSearchResponseDTO> getTrendingSeries() {

        return ResponseEntity.ok(tmdbApiService.trendingSeries());
    }

    @GetMapping("/trending/anime")
    public ResponseEntity<JikanSearchResponseDTO> getTrendingAnime() {

        return ResponseEntity.ok(jikanApiService.trendingAnime());
    }

    @GetMapping("/continue")
    public ResponseEntity<List<ContinueWatchingDTO>> getContinueWatching() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(watchlistService.getContinueWatching(username));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ContinueWatchingDTO>> getRecentlyAdded() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(watchlistService.getRecentlyAdded(username));
    }

}
