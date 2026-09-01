package com.example.todoapp.api;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;

@Component
public class HolidayClient {

    private static final String HOLIDAYS_URL =
            "https://holidays-jp.github.io/api/v1/date.json";

    private final RestClient restClient;

    public HolidayClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public Map<String, String> fetch() {
        return fetchWithStatus().holidays();
    }

    public FetchResult fetchWithStatus() {
        try {
            Map<String, String> holidays = restClient.get()
                    .uri(HOLIDAYS_URL)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            return new FetchResult(holidays == null ? Collections.emptyMap() : holidays, false);
        } catch (RestClientException e) {
            return new FetchResult(Collections.emptyMap(), true);
        }
    }

    public record FetchResult(Map<String, String> holidays, boolean unavailable) {
    }
}
