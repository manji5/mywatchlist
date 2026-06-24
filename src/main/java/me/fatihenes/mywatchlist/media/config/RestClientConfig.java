package me.fatihenes.mywatchlist.media.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.IOException;
import java.net.URI;

@Configuration
public class RestClientConfig {

    @Value("${application.tmdb.api-key}")
    private String tmdbApiKey;

    @Bean
    public RestClient jikanRestClient() {
        return RestClient.builder().baseUrl("https://api.jikan.moe/v4").build();
    }


    @Bean
    public RestClient tmdbRestClient() {
        return RestClient.builder().baseUrl("https://api.themoviedb.org/3")
                .requestInterceptor(new ClientHttpRequestInterceptor() {

                    @Override
                    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                            ClientHttpRequestExecution execution) throws IOException {
                        URI uri = UriComponentsBuilder.fromUri(request.getURI())
                                .queryParam("api_key", tmdbApiKey).build().toUri();

                        HttpRequest modifiedRequest = new HttpRequestWrapper(request) {
                            @Override
                            public URI getURI() {
                                return uri;
                            }
                        };
                        return execution.execute(modifiedRequest, body);
                    }

                }).build();

    }

}
