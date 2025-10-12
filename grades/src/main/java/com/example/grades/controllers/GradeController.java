package com.example.grades.controllers;

import com.example.grades.model.Grade;
import com.example.grades.repo.GradeRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/grades")
public class GradeController {

    private final GradeRepository repo;

    public GradeController(GradeRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("grades", repo.findAll());
        return "grades/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("grade", new Grade());
        model.addAttribute("title", "Добавить оценку");
        return "grades/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("grade") Grade grade,
                         BindingResult errors,
                         RedirectAttributes ra) {
        if (errors.hasErrors()) return "grades/form";
        repo.save(grade);
        ra.addFlashAttribute("msg", "Оценка добавлена");
        return "redirect:/grades";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Grade grade = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Не найден id=" + id));
        model.addAttribute("grade", grade);
        model.addAttribute("title", "Редактировать оценку");
        return "grades/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("grade") Grade grade,
                         BindingResult errors,
                         RedirectAttributes ra) {
        if (errors.hasErrors()) return "grades/form";
        grade.setId(id);
        repo.save(grade);
        ra.addFlashAttribute("msg", "Изменения сохранены");
        return "redirect:/grades";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        repo.deleteById(id);
        ra.addFlashAttribute("msg", "Запись удалена");
        return "redirect:/grades";
    }
}