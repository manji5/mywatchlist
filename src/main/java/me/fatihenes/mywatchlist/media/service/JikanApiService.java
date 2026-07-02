package me.fatihenes.mywatchlist.media.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import me.fatihenes.mywatchlist.exception.ExternalServiceException;
import me.fatihenes.mywatchlist.media.dto.JikanAnimeDetailDTO;
import me.fatihenes.mywatchlist.media.dto.JikanSearchResponseDTO;

@Service
public class JikanApiService {

    private final RestClient restClient;

    private record JikanDetailResponse(JikanAnimeDetailDTO data) {
    }

    public JikanApiService(@Qualifier("jikanRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public JikanSearchResponseDTO searchAnime(String query) {
        try {
            JikanSearchResponseDTO response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/anime").queryParam("q", query).build())
                    .retrieve().body(JikanSearchResponseDTO.class);

            if (response == null) {
                throw new ExternalServiceException("Jikan API returned an empty response.");
            }

            return response;
        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ExternalServiceException(
                    "Jikan API is currently unavailable: " + e.getMessage());
        }
    }

    public JikanSearchResponseDTO trendingAnime() {
        try {
            return restClient.get().uri("/top/anime").retrieve().body(JikanSearchResponseDTO.class);
        } catch (Exception e) {
            throw new ExternalServiceException("Jikan API is currently unavailable.");
        }
    }

    public JikanAnimeDetailDTO getAnime(Long id) {
        try {
            JikanDetailResponse response = restClient.get().uri("/anime/{id}", id).retrieve()
                    .body(JikanDetailResponse.class);

            if (response == null || response.data() == null) {
                throw new ExternalServiceException("Anime not found.");
            }

            return response.data();
        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ExternalServiceException("Jikan API is currently unavailable.");
        }
    }
}
