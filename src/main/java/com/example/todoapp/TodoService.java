package com.example.todoapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class TodoService {

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

    public Todo findById(Long id) {
        return todoMapper.findById(id);
    }

    public void create(Todo todo) {
        todoMapper.insert(todo);
        log.info("Todo created successfully: id={}", todo.getId());
    }

    public void update(Todo todo) {
        todoMapper.update(todo);
        log.info("Todo updated successfully: id={}", todo.getId());
    }

    public void delete(Long id) {
        todoMapper.deleteById(id);
        log.info("Todo deleted successfully: id={}", id);
    }
}
