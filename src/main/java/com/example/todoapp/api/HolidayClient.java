package com.example.todoapp.api;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class HolidayClient {

    private static final String HOLIDAYS_URL =
            "https://holidays-jp.github.io/api/v1/date.json";

    private final RestClient restClient;

    public HolidayClient() {
        this.restClient = RestClient.create();
    }

    public Map<String, String> fetch() {
        return restClient.get()
                .uri(HOLIDAYS_URL)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}
