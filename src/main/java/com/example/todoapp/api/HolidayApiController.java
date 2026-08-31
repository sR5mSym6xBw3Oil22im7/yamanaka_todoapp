package com.example.todoapp.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public Map<String, String> holidays(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        Map<String, String> holidays = holidayClient.fetch();

        return holidays.entrySet().stream()
                .filter(entry -> isInRange(entry.getKey(), from, to))
                .collect(LinkedHashMap::new,
                        (result, entry) -> result.put(entry.getKey(), entry.getValue()),
                        LinkedHashMap::putAll);
    }

    private boolean isInRange(String dateText, LocalDate from, LocalDate to) {
        LocalDate date = LocalDate.parse(dateText);
        return (from == null || !date.isBefore(from))
                && (to == null || !date.isAfter(to));
    }
}
