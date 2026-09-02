package com.example.todoapp.mcp;

import com.example.todoapp.Todo;
import com.example.todoapp.TodoService;
import com.example.todoapp.api.HolidayClient;
import com.example.todoapp.api.TodoDto;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class TodoTools {

    private final TodoService todoService;
    private final HolidayClient holidayClient;

    public TodoTools(TodoService todoService, HolidayClient holidayClient) {
        this.todoService = todoService;
        this.holidayClient = holidayClient;
    }

    @McpTool(name = "list_todos", description = "やることの一覧を返す（期間・ジャンルで絞れる）")
    public List<TodoDto> listTodos(
            @McpToolParam(description = "キーワード", required = false) String keyword,
            @McpToolParam(description = "ジャンル", required = false) String category,
            @McpToolParam(description = "開始日（yyyy-MM-dd）", required = false) String from,
            @McpToolParam(description = "終了日（yyyy-MM-dd）", required = false) String to) {
        LocalDate fromDate = from == null ? null : LocalDate.parse(from);
        LocalDate toDate = to == null ? null : LocalDate.parse(to);

        return todoService.search(keyword, category, "asc", fromDate, toDate)
                .stream()
                .map(TodoDto::from)
                .toList();
    }

    @McpTool(name = "get_todo", description = "やることを1件返す")
    public TodoDto getTodo(
            @McpToolParam(description = "やることのID", required = true) Long id) {
        return TodoDto.from(todoService.findById(id));
    }
    @McpTool(name = "add_todo", description = "やることを1件足す")
    public TodoDto addTodo(
            @McpToolParam(description = "やることのタイトル", required = true) String title,
            @McpToolParam(description = "メモ", required = false) String detail,
            @McpToolParam(description = "ジャンル", required = true) String category,
            @McpToolParam(description = "優先度", required = true) Integer priority,
            @McpToolParam(description = "期限（yyyy-MM-dd）", required = false) String dueDate,
            @McpToolParam(description = "完了済みかどうか", required = false) Boolean completed) {
        validateCategory(category);

        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setDetail(detail);
        todo.setCategory(category);
        todo.setPriority(priority);
        todo.setDueDate(dueDate == null ? null : LocalDate.parse(dueDate));
        todo.setCompleted(completed);
        todoService.create(todo);
        return TodoDto.from(todo);
    }

    @McpTool(name = "update_todo", description = "やることを1件直す（期限を変えるのもこれ）")
    public TodoDto updateTodo(
            @McpToolParam(description = "やることのID", required = true) Long id,
            @McpToolParam(description = "やることのタイトル", required = false) String title,
            @McpToolParam(description = "メモ", required = false) String detail,
            @McpToolParam(description = "ジャンル", required = false) String category,
            @McpToolParam(description = "優先度", required = false) Integer priority,
            @McpToolParam(description = "期限（yyyy-MM-dd）", required = false) String dueDate,
            @McpToolParam(description = "完了済みかどうか", required = false) Boolean completed) {
        Todo todo = todoService.findById(id);
        if (todo == null) return null;

        if (title != null) todo.setTitle(title);
        if (detail != null) todo.setDetail(detail);
        if (category != null) {
            validateCategory(category);
            todo.setCategory(category);
        }
        if (priority != null) todo.setPriority(priority);
        if (dueDate != null) todo.setDueDate(LocalDate.parse(dueDate));
        if (completed != null) todo.setCompleted(completed);

        todoService.update(todo);
        return TodoDto.from(todo);
    }

    @McpTool(name = "complete_todo", description = "やることを完了にする")
    public TodoDto completeTodo(
            @McpToolParam(description = "やることのID", required = true) Long id) {
        Todo todo = todoService.findById(id);
        if (todo == null) return null;

        todo.setCompleted(true);
        todoService.update(todo);
        return TodoDto.from(todo);
    }

    @McpTool(name = "delete_todo", description = "やることを1件消す")
    public void deleteTodo(
            @McpToolParam(description = "やることのID", required = true) Long id) {
        todoService.delete(id);
    }

    @McpTool(name = "summarize_week", description = "期間内のやることを一覧ではなく、総件数・ジャンルごとの件数・期限が過ぎた未完了の件数に数えて要約する")
    public TodoSummary summarizeWeek(
            @McpToolParam(description = "期間の始まり（yyyy-MM-dd）", required = false) String from,
            @McpToolParam(description = "期間の終わり（yyyy-MM-dd）", required = false) String to) {
        LocalDate fromDate = from == null ? null : LocalDate.parse(from);
        LocalDate toDate = to == null ? null : LocalDate.parse(to);
        List<Todo> todos = todoService.search(null, null, "asc", fromDate, toDate);

        Map<String, Integer> categoryBreakdown = new LinkedHashMap<>();
        int overdueCount = 0;
        LocalDate today = LocalDate.now();
        for (Todo todo : todos) {
            categoryBreakdown.merge(todo.getCategory(), 1, Integer::sum);
            if (todo.getDueDate() != null
                    && todo.getDueDate().isBefore(today)
                    && !Boolean.TRUE.equals(todo.getCompleted())) {
                overdueCount++;
            }
        }
        return new TodoSummary(todos.size(), categoryBreakdown, overdueCount);
    }

    public record TodoSummary(
            int totalCount,
            Map<String, Integer> categoryBreakdown,
            int overdueCount) {
    }

    @McpTool(name = "find_free_days", description = "期間の中で、期限のやることが無く、土日でも祝日でもない「空いている日」を返す。やることの期限を動かす先を決めるのに使う")
    public List<LocalDate> findFreeDays(
            @McpToolParam(description = "開始日（yyyy-MM-dd）", required = true) String from,
            @McpToolParam(description = "終了日（yyyy-MM-dd）", required = true) String to) {
        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate = LocalDate.parse(to);

        Set<LocalDate> dueDates = new HashSet<>(todoService.search(null, null, "asc", fromDate, toDate)
                .stream()
                .map(Todo::getDueDate)
                .filter(java.util.Objects::nonNull)
                .toList());
        Set<String> holidays = holidayClient.fetchWithStatus().holidays().keySet();

        List<LocalDate> freeDays = new java.util.ArrayList<>();
        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY
                    || date.getDayOfWeek() == DayOfWeek.SUNDAY) continue;
            if (dueDates.contains(date) || holidays.contains(date.toString())) continue;
            freeDays.add(date);
        }
        return freeDays;
    }

    private void validateCategory(String category) {
        if (!Set.of("デザイン", "マーケティング", "プログラミング", "資格", "就職活動")
                .contains(category)) {
            throw new IllegalArgumentException("ジャンルは指定された5種類のいずれかにしてください");
        }
    }
}
