package com.example.todoapp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CalendarController {

    @GetMapping({"/calendar", "/calender"})
    public String calendar(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Model model) {
        LocalDate today = LocalDate.now();
        YearMonth displayedMonth = YearMonth.of(
                year != null ? year : today.getYear(),
                month != null ? month : today.getMonthValue());

        LocalDate firstDay = displayedMonth.atDay(1);
        LocalDate lastDay = displayedMonth.atEndOfMonth();
        LocalDate calendarStart = firstDay.minusDays(firstDay.getDayOfWeek().getValue() % 7);
        LocalDate calendarEnd = lastDay.plusDays((DayOfWeek.SATURDAY.getValue()
                - lastDay.getDayOfWeek().getValue() + 7) % 7);

        List<LocalDate> calendarDays = new ArrayList<>();
        for (LocalDate day = calendarStart; !day.isAfter(calendarEnd); day = day.plusDays(1)) {
            calendarDays.add(YearMonth.from(day).equals(displayedMonth)
                    ? day
                    : null);
        }

        List<List<LocalDate>> calendarWeeks = new ArrayList<>();
        for (int index = 0; index < calendarDays.size(); index += 7) {
            calendarWeeks.add(calendarDays.subList(index, index + 7));
        }

        YearMonth previousMonth = displayedMonth.minusMonths(1);
        YearMonth nextMonth = displayedMonth.plusMonths(1);
        model.addAttribute("displayedMonth", displayedMonth);
        model.addAttribute("year", displayedMonth.getYear());
        model.addAttribute("month", displayedMonth.getMonthValue());
        model.addAttribute("firstDay", firstDay);
        model.addAttribute("lastDay", lastDay);
        model.addAttribute("calendarDays", calendarDays);
        model.addAttribute("calendarWeeks", calendarWeeks);
        model.addAttribute("previousYear", previousMonth.getYear());
        model.addAttribute("previousMonth", previousMonth.getMonthValue());
        model.addAttribute("nextYear", nextMonth.getYear());
        model.addAttribute("nextMonth", nextMonth.getMonthValue());
        return "calendar";
    }
}
