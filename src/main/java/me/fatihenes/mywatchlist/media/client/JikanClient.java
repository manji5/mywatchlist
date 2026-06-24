package me.fatihenes.mywatchlist.media.client;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import lombok.RequiredArgsConstructor;
import me.fatihenes.mywatchlist.exception.ExternalServiceException;
import me.fatihenes.mywatchlist.exception.ResourceNotFoundException;
import me.fatihenes.mywatchlist.media.dto.JikanAnimeDTO;
import me.fatihenes.mywatchlist.media.dto.JikanResponseDTO;

@Component
@RequiredArgsConstructor
public class JikanClient {

    private final RestClient jikanRestClient;

    @Cacheable(value = "anime", key = "#malId")
    public JikanAnimeDTO fetchAnimeDetails(Long malId) {
        try {
            JikanResponseDTO response = jikanRestClient.get().uri("/anime/{id}", malId).retrieve()
                    .body(JikanResponseDTO.class);

            if (response == null || response.getData() == null) {
                throw new ResourceNotFoundException("Anime not found in Jikan API.");
            }

            return response.getData();
        } catch (Exception e) {
            throw new ExternalServiceException(
                    "Jikan API is currently unavailable or the anime does not exist.");
        }
    }
}
