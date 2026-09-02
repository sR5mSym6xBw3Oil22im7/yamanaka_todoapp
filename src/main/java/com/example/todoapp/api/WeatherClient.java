package com.example.todoapp.api;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.json.JsonMapper;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class WeatherClient {

    private static final Logger logger = LoggerFactory.getLogger(WeatherClient.class);

    private static final String WEATHER_URL =
            "https://api.open-meteo.com/v1/forecast?latitude=35.6895&longitude=139.6917"
                    + "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max"
                    + "&timezone=Asia/Tokyo&forecast_days=16";

    private final RestClient restClient;
    private final JsonMapper jsonMapper;

    public WeatherClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
        this.jsonMapper = JsonMapper.builder().build();
    }

    public FetchResult fetchWithStatus() {
        try {
            String responseBody = restClient.get()
                    .uri(WEATHER_URL)
                    .retrieve()
                    .body(String.class);
            Map<String, Object> response = jsonMapper.readValue(responseBody, Map.class);
            if (response == null || !(response.get("daily") instanceof Map<?, ?> daily)) {
                return new FetchResult(Collections.emptyMap(), true);
            }

            List<?> dates = listValue(daily, "time");
            Map<String, Weather> weatherByDate = new LinkedHashMap<>();
            int count = dates == null ? 0 : dates.size();
            for (int index = 0; index < count; index++) {
                weatherByDate.put(String.valueOf(dates.get(index)), new Weather(
                        integerAt(daily, "weather_code", index),
                        doubleAt(daily, "temperature_2m_max", index),
                        doubleAt(daily, "temperature_2m_min", index),
                        integerAt(daily, "precipitation_probability_max", index)));
            }
            return new FetchResult(weatherByDate, false);
        } catch (RuntimeException e) {
            logger.warn("天気情報を取得できませんでした", e);
            return new FetchResult(Collections.emptyMap(), true);
        }
    }

    private List<?> listValue(Map<?, ?> values, String key) {
        return values.get(key) instanceof List<?> list ? list : null;
    }

    private Integer integerAt(Map<?, ?> values, String key, int index) {
        List<?> list = listValue(values, key);
        return list != null && index < list.size() && list.get(index) instanceof Number number
                ? number.intValue()
                : null;
    }

    private Double doubleAt(Map<?, ?> values, String key, int index) {
        List<?> list = listValue(values, key);
        return list != null && index < list.size() && list.get(index) instanceof Number number
                ? number.doubleValue()
                : null;
    }

    public record FetchResult(Map<String, Weather> weather, boolean unavailable) {
    }

    public record Weather(Integer weatherCode, Double temperatureMax, Double temperatureMin,
                          Integer precipitationProbability) {
    }

}
