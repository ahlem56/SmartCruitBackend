package tn.esprit.examen.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.entities.Candidate;

import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate,Long> {
    Optional<Candidate> findByEmail(String email);

}
