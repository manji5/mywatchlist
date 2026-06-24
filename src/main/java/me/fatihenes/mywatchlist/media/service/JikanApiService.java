package me.fatihenes.mywatchlist.media.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import me.fatihenes.mywatchlist.media.dto.JikanSearchResponseDTO;

@Service
public class JikanApiService {
    private final RestClient restClient;

    public JikanApiService(@Qualifier("jikanRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public JikanSearchResponseDTO searchAnime(String query) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/anime").queryParam("q", query).build())
                .retrieve().body(JikanSearchResponseDTO.class);
    }

}
