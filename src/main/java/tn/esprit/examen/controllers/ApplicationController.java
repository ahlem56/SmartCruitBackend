package tn.esprit.examen.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.entities.Application;
import tn.esprit.examen.services.ApplicationService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/application")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/create")
    public ResponseEntity<Application> create(@RequestBody Application application) {
        return ResponseEntity.ok(applicationService.create(application));
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<Application>> getAll() {
        return ResponseEntity.ok(applicationService.getAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Application> getById(@PathVariable Long id) {
        return applicationService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        applicationService.delete(id);
        return ResponseEntity.ok().build();
    }
}
