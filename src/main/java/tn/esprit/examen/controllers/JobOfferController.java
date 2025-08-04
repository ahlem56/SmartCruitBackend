package tn.esprit.examen.controllers;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;
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

    @PreAuthorize("hasRole('EMPLOYER')")
    @PostMapping("/create")
    public ResponseEntity<JobOffer> create( @RequestBody JobOffer jobOffer) {
        System.out.println("Received category: " + jobOffer.getCategory());
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

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        jobOfferService.delete(id);
        return ResponseEntity.ok().build();
    }
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/update/{id}")
    public ResponseEntity<JobOffer> update(@PathVariable Long id, @RequestBody JobOffer updatedOffer) {
        return jobOfferService.getById(id)
                .map(existing -> {
                    updatedOffer.setJobOfferId(existing.getJobOfferId());
                    return ResponseEntity.ok(jobOfferService.create(updatedOffer));
                })
                .orElse(ResponseEntity.notFound().build());
    }



}
