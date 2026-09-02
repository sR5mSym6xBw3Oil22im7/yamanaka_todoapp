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
            "https://www.jma.go.jp/bosai/forecast/data/forecast/130000.json";

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
            List<?> forecasts = jsonMapper.readValue(responseBody, List.class);
            if (forecasts == null || forecasts.isEmpty()) {
                return new FetchResult(Collections.emptyMap(), true);
            }

            Map<String, Weather> weatherByDate = new LinkedHashMap<>();
            for (Object forecastValue : forecasts) {
                if (forecastValue instanceof Map<?, ?> forecast) {
                    readTimeSeries(forecast, weatherByDate);
                }
            }
            if (weatherByDate.isEmpty()) {
                return new FetchResult(Collections.emptyMap(), true);
            }
            return new FetchResult(weatherByDate, false);
        } catch (RuntimeException e) {
            logger.warn("天気情報を取得できませんでした", e);
            return new FetchResult(Collections.emptyMap(), true);
        }
    }

    private void readTimeSeries(Map<?, ?> forecast, Map<String, Weather> weatherByDate) {
        List<?> timeSeries = listValue(forecast, "timeSeries");
        if (timeSeries == null) {
            return;
        }
        for (Object seriesValue : timeSeries) {
            if (!(seriesValue instanceof Map<?, ?> series)) {
                continue;
            }
            List<?> dates = listValue(series, "timeDefines");
            Map<?, ?> area = findArea(series);
            if (dates == null || area == null) {
                continue;
            }

            List<?> weatherCodes = listValue(area, "weatherCodes");
            List<?> weathers = listValue(area, "weathers");
            List<?> precipitationProbabilities = listValue(area, "pops");
            List<?> maxTemperatures = listValue(area, "tempsMax");
            List<?> minTemperatures = listValue(area, "tempsMin");

            for (int index = 0; index < dates.size(); index++) {
                String date = dateOnly(dates.get(index));
                if (date == null) {
                    continue;
                }
                Weather current = weatherByDate.get(date);
                Integer weatherCode = current == null ? null : current.weatherCode();
                String weatherText = current == null ? null : current.weatherText();
                Integer precipitationProbability = current == null
                        ? null : current.precipitationProbability();
                Double temperatureMax = current == null ? null : current.temperatureMax();
                Double temperatureMin = current == null ? null : current.temperatureMin();

                if (weatherCodes != null) {
                    weatherCode = integerValueAt(weatherCodes, index);
                }
                if (weathers != null) {
                    weatherText = stringValueAt(weathers, index);
                }
                if (precipitationProbabilities != null) {
                    precipitationProbability = integerValueAt(precipitationProbabilities, index);
                }
                if (maxTemperatures != null) {
                    temperatureMax = doubleValueAt(maxTemperatures, index);
                }
                if (minTemperatures != null) {
                    temperatureMin = doubleValueAt(minTemperatures, index);
                }

                if (weatherCode != null || weatherText != null || precipitationProbability != null
                        || temperatureMax != null || temperatureMin != null) {
                    weatherByDate.put(date, new Weather(weatherCode, weatherText, temperatureMax,
                            temperatureMin, precipitationProbability));
                }
            }
        }
    }

    private Map<?, ?> findArea(Map<?, ?> series) {
        List<?> areas = listValue(series, "areas");
        if (areas == null) {
            return null;
        }
        for (Object areaValue : areas) {
            if (!(areaValue instanceof Map<?, ?> area)) {
                continue;
            }
            Object areaInfo = area.get("area");
            if (areaInfo instanceof Map<?, ?> areaDetails
                    && ("130010".equals(String.valueOf(areaDetails.get("code")))
                    || "44132".equals(String.valueOf(areaDetails.get("code"))))) {
                return area;
            }
        }
        return null;
    }

    private List<?> listValue(Map<?, ?> values, String key) {
        return values.get(key) instanceof List<?> list ? list : null;
    }

    private String dateOnly(Object value) {
        if (value == null) {
            return null;
        }
        String dateTime = String.valueOf(value);
        return dateTime.length() >= 10 ? dateTime.substring(0, 10) : null;
    }

    private Integer integerValueAt(List<?> values, int index) {
        if (index >= values.size() || values.get(index) == null || String.valueOf(values.get(index)).isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(String.valueOf(values.get(index)));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double doubleValueAt(List<?> values, int index) {
        if (index >= values.size() || values.get(index) == null || String.valueOf(values.get(index)).isBlank()) {
            return null;
        }
        try {
            return Double.valueOf(String.valueOf(values.get(index)));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String stringValueAt(List<?> values, int index) {
        return index < values.size() && values.get(index) != null
                ? String.valueOf(values.get(index)).trim()
                : null;
    }

    public record FetchResult(Map<String, Weather> weather, boolean unavailable) {
    }

    public record Weather(Integer weatherCode, String weatherText, Double temperatureMax, Double temperatureMin,
                          Integer precipitationProbability) {
    }

}
