package com.example.todoapp.api;

import com.example.todoapp.TodoService;
import com.example.todoapp.Todo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TodoApiController {

    private final TodoService todoService;

    public TodoApiController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/api/todos")
    public List<TodoDto> todos(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "asc") String order) {
        return todoService.search(keyword, category, order)
                .stream()
                .map(TodoDto::from)
                .toList();
    }

    @GetMapping("/api/todos/{id}")
    public ResponseEntity<?> todo(@PathVariable Long id) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            return notFound(id);
        }
        return ResponseEntity.ok(TodoDto.from(todo));
    }

    @PostMapping(value = "/api/todos", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(
            @Valid @RequestBody TodoRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {
        if (bindingResult.hasErrors()) {
            return badRequest(bindingResult, httpRequest);
        }
        Todo todo = request.toTodo();
        todoService.create(todo);
        Todo created = todoService.findById(todo.getId());
        return ResponseEntity.created(URI.create("/api/todos/" + todo.getId()))
                .body(TodoDto.from(created));
    }

    @PutMapping(value = "/api/todos/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @Valid @RequestBody TodoRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {
        if (bindingResult.hasErrors()) {
            return badRequest(bindingResult, httpRequest);
        }
        Todo todo = request.toTodo();
        Todo existing = todoService.findById(id);
        if (existing == null) {
            return notFound(id);
        }
        todo.setId(id);
        todoService.update(todo);
        return ResponseEntity.ok(TodoDto.from(todoService.findById(id)));
    }

    @DeleteMapping("/api/todos/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Todo existing = todoService.findById(id);
        if (existing == null) {
            return notFound(id);
        }
        todoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<ProblemDetail> notFound(Long id) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, "Todo not found: " + id);
        problem.setType(URI.create("about:blank"));
        problem.setTitle("Todo not found");
        problem.setInstance(URI.create("/api/todos/" + id));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    private ResponseEntity<ProblemDetail> badRequest(
            BindingResult bindingResult, HttpServletRequest httpRequest) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "入力に誤りがあります");
        problem.setType(URI.create("about:blank"));
        problem.setTitle("Bad Request");
        problem.setInstance(URI.create(httpRequest.getRequestURI()));

        List<Map<String, String>> errors = new ArrayList<>();
        bindingResult.getFieldErrors().forEach(error -> {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("field", error.getField());
            item.put("message", error.getDefaultMessage());
            errors.add(item);
        });
        problem.setProperty("errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }
}
