package tn.esprit.examen.controllers;
import org.springframework.security.access.prepost.PreAuthorize;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.entities.Candidate;
import tn.esprit.examen.repositories.CandidateRepository;
import tn.esprit.examen.services.ICandidateService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("candidate")
public class CandidateController {

    private final CandidateRepository candidateRepository;

    private final ICandidateService candidateService;
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<Candidate>> getAllCandidates() {
        return ResponseEntity.ok(candidateService.getAllCandidates());
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<Candidate> updateCandidate(@PathVariable Long id, @RequestBody Candidate candidate) {
        Candidate updated = candidateService.updateCandidate(id, candidate);
        return updated != null
                ? ResponseEntity.ok(updated)
                : ResponseEntity.notFound().build();
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCandidate(@PathVariable Long id) {
        candidateService.deleteCandidate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public long getCandidateCount() {
        return candidateRepository.count();
    }

}
