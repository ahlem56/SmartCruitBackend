package tn.esprit.examen.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.entities.JobOffer;
import tn.esprit.examen.services.JobOfferService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/jobOffer")
public class JobOfferController {

    private JobOfferService jobOfferService;

    @PostMapping("/create")
    public ResponseEntity<JobOffer> create(@RequestBody JobOffer jobOffer) {
        return ResponseEntity.ok(jobOfferService.create(jobOffer));
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<JobOffer>> getAll() {
        return ResponseEntity.ok(jobOfferService.getAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<JobOffer> getById(@PathVariable Long id) {
        return jobOfferService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        jobOfferService.delete(id);
        return ResponseEntity.ok().build();
    }
}
