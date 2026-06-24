package me.fatihenes.mywatchlist.media.client;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import lombok.RequiredArgsConstructor;
import me.fatihenes.mywatchlist.exception.ExternalServiceException;
import me.fatihenes.mywatchlist.media.dto.TmdbDTO;

@Component
@RequiredArgsConstructor
public class TmdbClient {

    private final RestClient tmdbRestClient;

    @Cacheable(value = "movies", key = "#movieId")
    public TmdbDTO fetchMovieDetails(Long movieId) {
        try {
            return tmdbRestClient.get().uri("/movie/{id}", movieId).retrieve().body(TmdbDTO.class);
        } catch (Exception e) {
            throw new ExternalServiceException(
                    "TMDB is currently unavailable or the movie does not exist.");
        }
    }

    @Cacheable(value = "series", key = "#seriesId")
    public TmdbDTO fetchSeriesDetails(Long seriesId) {
        try {
            return tmdbRestClient.get().uri("/tv/{id}", seriesId).retrieve().body(TmdbDTO.class);
        } catch (Exception e) {
            throw new ExternalServiceException(
                    "TMDB is currently unavailable or the TV series does not exist.");
        }
    }
}
