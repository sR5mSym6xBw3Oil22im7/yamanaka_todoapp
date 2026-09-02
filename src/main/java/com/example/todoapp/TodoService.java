package com.example.todoapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class TodoService {

    public static final int PAGE_SIZE = 10;

    private final TodoMapper todoMapper;

    public TodoService(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    public List<Todo> search(String keyword, String category, String order) {
        return search(keyword, category, order, null, null);
    }

    public List<Todo> search(String keyword, String category, String order,
                             LocalDate from, LocalDate to) {
        return todoMapper.search(keyword, category, order, from, to);
    }

    public List<Todo> searchPage(String keyword, String category, String order,
                                 LocalDate from, LocalDate to, int page) {
        return todoMapper.searchPage(keyword, category, order, from, to,
                PAGE_SIZE, (page - 1) * PAGE_SIZE);
    }

    public int countSearch(String keyword, String category, LocalDate from, LocalDate to) {
        return todoMapper.countSearch(keyword, category, from, to);
    }

    public Todo findById(Long id) {
        return todoMapper.findById(id);
    }

    public void create(Todo todo) {
        todoMapper.insert(todo);
        log.info("Todo created successfully: id={}", todo.getId());
    }

    public void update(Todo todo) {
        Todo existing = todoMapper.findById(todo.getId());
        boolean wasCompleted = Boolean.TRUE.equals(existing.getCompleted());
        boolean isCompleted = Boolean.TRUE.equals(todo.getCompleted());
        if (!wasCompleted && isCompleted) {
            todo.setCompletedAt(LocalDateTime.now());
        } else if (wasCompleted && !isCompleted) {
            todo.setCompletedAt(null);
        } else {
            todo.setCompletedAt(existing.getCompletedAt());
        }
        todoMapper.update(todo);
        log.info("Todo updated successfully: id={}", todo.getId());
    }

    public void delete(Long id) {
        todoMapper.deleteById(id);
        log.info("Todo deleted successfully: id={}", id);
    }
}
