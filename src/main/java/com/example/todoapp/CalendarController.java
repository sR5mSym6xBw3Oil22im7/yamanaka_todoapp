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

    @GetMapping("/calendar")
    public String calendar(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day,
            @RequestParam(defaultValue = "month") String view,
            Model model) {
        LocalDate today = LocalDate.now();
        boolean weekView = "week".equalsIgnoreCase(view);

        if (weekView) {
            LocalDate selectedDay = day != null
                    ? LocalDate.of(year != null ? year : today.getYear(),
                    month != null ? month : today.getMonthValue(), day)
                    : today;
            LocalDate weekStart = selectedDay.minusDays(selectedDay.getDayOfWeek().getValue() % 7);
            LocalDate weekEnd = weekStart.plusDays(6);
            List<LocalDate> calendarDays = new ArrayList<>();
            for (LocalDate current = weekStart; !current.isAfter(weekEnd); current = current.plusDays(1)) {
                calendarDays.add(current);
            }

            model.addAttribute("weekView", true);
            model.addAttribute("weekStart", weekStart);
            model.addAttribute("weekEnd", weekEnd);
            model.addAttribute("firstDay", weekStart);
            model.addAttribute("lastDay", weekEnd);
            model.addAttribute("calendarDays", calendarDays);
            model.addAttribute("calendarWeeks", List.of(calendarDays));
            model.addAttribute("previousWeekStart", weekStart.minusDays(7));
            model.addAttribute("nextWeekStart", weekStart.plusDays(7));
            model.addAttribute("year", selectedDay.getYear());
            model.addAttribute("month", selectedDay.getMonthValue());
            return "calendar";
        }

        YearMonth displayedMonth = YearMonth.of(
                year != null ? year : today.getYear(),
                month != null ? month : today.getMonthValue());

        LocalDate firstDay = displayedMonth.atDay(1);
        LocalDate lastDay = displayedMonth.atEndOfMonth();
        LocalDate calendarStart = firstDay.minusDays(firstDay.getDayOfWeek().getValue() % 7);
        LocalDate calendarEnd = lastDay.plusDays((DayOfWeek.SATURDAY.getValue()
                - lastDay.getDayOfWeek().getValue() + 7) % 7);

        List<LocalDate> calendarDays = new ArrayList<>();
        for (LocalDate calendarDay = calendarStart;
             !calendarDay.isAfter(calendarEnd);
             calendarDay = calendarDay.plusDays(1)) {
            calendarDays.add(YearMonth.from(calendarDay).equals(displayedMonth)
                    ? calendarDay
                    : null);
        }

        List<List<LocalDate>> calendarWeeks = new ArrayList<>();
        for (int index = 0; index < calendarDays.size(); index += 7) {
            calendarWeeks.add(calendarDays.subList(index, index + 7));
        }

        YearMonth previousMonth = displayedMonth.minusMonths(1);
        YearMonth nextMonth = displayedMonth.plusMonths(1);
        model.addAttribute("displayedMonth", displayedMonth);
        model.addAttribute("weekView", false);
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
