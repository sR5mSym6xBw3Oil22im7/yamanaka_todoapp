package com.example.todoapp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class HomeController {

    private final TodoMapper todoMapper;

    public HomeController(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "やること管理");
        return "index";
    }
    @GetMapping("/todos")
    public String todos(Model model) {
        List<Todo> todos = todoMapper.findAll();
        model.addAttribute("todos", todos);
        return "todos";
    }

    @GetMapping("/todos/new")
    public String create(Model model) {
        model.addAttribute("todo", new Todo());
        return "create";
    }

    @PostMapping("/todos/confirm")
    public String createConfirm(@ModelAttribute Todo todo, BindingResult bindingResult, Model model) {
        validateCreate(todo, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("todo", todo);
            return "create";
        }
        model.addAttribute("todo", todo);
        return "create-confirm";
    }

    @PostMapping("/todos/new")
    public String createBack(@ModelAttribute Todo todo, Model model) {
        model.addAttribute("todo", todo);
        return "create";
    }

    @PostMapping("/todos")
    public String save(@ModelAttribute Todo todo, RedirectAttributes redirectAttributes) {
        // The confirmation form can be submitted directly, so keep the DB-safe
        // default here as well as in the input screen.
        if (todo.getPriority() == null) {
            todo.setPriority(2);
        }
        todoMapper.insert(todo);
        redirectAttributes.addFlashAttribute("message", "登録しました");
        return "redirect:/todos";
    }

    private void validateCreate(Todo todo, BindingResult bindingResult) {
        if (todo.getTitle() == null || todo.getTitle().isBlank()) {
            bindingResult.rejectValue("title", "required", "やることを入力してください");
        }
        if (todo.getCategory() == null || todo.getCategory().isBlank()) {
            bindingResult.rejectValue("category", "required", "ジャンルを選択してください");
        }
        if (todo.getPriority() != null && (todo.getPriority() < 1 || todo.getPriority() > 3)) {
            bindingResult.rejectValue("priority", "invalid", "優先度を選択してください");
        }
    }

    @GetMapping("/todos/{id}/edit")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Todo todo = todoMapper.findById(id);
        if (todo == null) {
            redirectAttributes.addFlashAttribute("message", "見つかりませんでした");
            return "redirect:/todos";
        }
        model.addAttribute("todo", todo);
        model.addAttribute("id", id);
        return "edit";
    }

    @PostMapping("/todos/{id}/confirm")
    public String editConfirm(@PathVariable Long id, @ModelAttribute Todo todo, Model model) {
        model.addAttribute("todo", todo);
        model.addAttribute("id", id);
        return "edit-confirm";
    }

    @PostMapping("/todos/{id}/edit")
    public String editBack(@PathVariable Long id, @ModelAttribute Todo todo, Model model) {
        model.addAttribute("todo", todo);
        model.addAttribute("id", id);
        return "edit";
    }

    @PostMapping("/todos/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Todo todo,
                         RedirectAttributes redirectAttributes) {
        todo.setId(id);
        todoMapper.update(todo);
        redirectAttributes.addFlashAttribute("message", "保存しました");
        return "redirect:/todos";
    }
}
