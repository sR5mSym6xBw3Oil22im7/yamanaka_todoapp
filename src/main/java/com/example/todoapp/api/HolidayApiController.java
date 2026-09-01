package com.example.todoapp.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HolidayApiController {

    private final HolidayClient holidayClient;

    public HolidayApiController(HolidayClient holidayClient) {
        this.holidayClient = holidayClient;
    }

    @GetMapping("/api/holidays")
    public ResponseEntity<Map<String, String>> holidays(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        HolidayClient.FetchResult fetchResult = holidayClient.fetchWithStatus();
        Map<String, String> holidays = fetchResult.holidays();

        Map<String, String> result = holidays.entrySet().stream()
                .filter(entry -> isInRange(entry.getKey(), from, to))
                .collect(LinkedHashMap::new,
                        (collected, entry) -> collected.put(entry.getKey(), entry.getValue()),
                        LinkedHashMap::putAll);

        ResponseEntity.BodyBuilder response = ResponseEntity.ok();
        if (fetchResult.unavailable()) {
            response.header("X-Holidays-Unavailable", "true");
        }
        return response.body(result);
    }

    private boolean isInRange(String dateText, LocalDate from, LocalDate to) {
        LocalDate date = LocalDate.parse(dateText);
        return (from == null || !date.isBefore(from))
                && (to == null || !date.isAfter(to));
    }
}
