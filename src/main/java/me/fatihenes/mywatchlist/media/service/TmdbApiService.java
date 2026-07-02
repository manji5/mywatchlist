package me.fatihenes.mywatchlist.media.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import me.fatihenes.mywatchlist.media.dto.TmdbDTO;
import me.fatihenes.mywatchlist.media.dto.TmdbDetailDTO;
import me.fatihenes.mywatchlist.media.dto.TmdbSearchResponseDTO;

@Service
public class TmdbApiService {

    private final RestClient restClient;

    public TmdbApiService(@Qualifier("tmdbRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public TmdbSearchResponseDTO searchMovie(String query) {
        return restClient.get().uri(
                uriBuilder -> uriBuilder.path("/search/movie").queryParam("query", query).build())
                .retrieve().body(TmdbSearchResponseDTO.class);
    }

    public TmdbSearchResponseDTO searchSeries(String query) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/search/tv").queryParam("query", query).build())
                .retrieve().body(TmdbSearchResponseDTO.class);
    }

    public TmdbSearchResponseDTO trendingMovies() {
        return restClient.get().uri("/trending/movie/week").retrieve()
                .body(TmdbSearchResponseDTO.class);
    }

    public TmdbSearchResponseDTO trendingSeries() {
        return restClient.get().uri("/trending/tv/week").retrieve()
                .body(TmdbSearchResponseDTO.class);
    }

    public TmdbDetailDTO getMovie(Long id) {

        return restClient.get().uri("/movie/{id}", id).retrieve().body(TmdbDetailDTO.class);

    }

    public TmdbDetailDTO getSeries(Long id) {

        return restClient.get().uri("/tv/{id}", id).retrieve().body(TmdbDetailDTO.class);

    }

}
