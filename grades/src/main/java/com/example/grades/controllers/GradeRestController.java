package com.example.grades.controllers;

import com.example.grades.model.Grade;
import com.example.grades.repo.GradeRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/grades")
@CrossOrigin(origins = "*") // если не нужно — убери или ограничь до "http://localhost"
public class GradeRestController {

    private final GradeRepository repo;

    public GradeRestController(GradeRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Grade> list() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Grade get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Grade not found: " + id));
    }

    @PostMapping
    public ResponseEntity<Grade> create(@Valid @RequestBody Grade grade) {
        grade.setId(null);
        Grade saved = repo.save(grade);
        return ResponseEntity
                .created(URI.create("/api/grades/" + saved.getId()))
                .body(saved);
    }

    @PutMapping("/{id}")
    public Grade update(@PathVariable Long id, @Valid @RequestBody Grade grade) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Grade not found: " + id);
        }
        grade.setId(id);
        return repo.save(grade);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
