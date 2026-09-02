package com.example.todoapp.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class WeatherApiController {

    private final WeatherClient weatherClient;

    public WeatherApiController(WeatherClient weatherClient) {
        this.weatherClient = weatherClient;
    }

    @GetMapping("/api/weather")
    public ResponseEntity<Map<String, WeatherClient.Weather>> weather(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        WeatherClient.FetchResult fetchResult = weatherClient.fetchWithStatus();
        Map<String, WeatherClient.Weather> result = fetchResult.weather().entrySet().stream()
                .filter(entry -> isInRange(entry.getKey(), from, to))
                .collect(LinkedHashMap::new,
                        (collected, entry) -> collected.put(entry.getKey(), entry.getValue()),
                        LinkedHashMap::putAll);

        ResponseEntity.BodyBuilder response = ResponseEntity.ok();
        if (fetchResult.unavailable()) {
            response.header("X-Weather-Unavailable", "true");
        }
        return response.body(result);
    }

    private boolean isInRange(String dateText, LocalDate from, LocalDate to) {
        LocalDate date = LocalDate.parse(dateText);
        return (from == null || !date.isBefore(from))
                && (to == null || !date.isAfter(to));
    }
}
