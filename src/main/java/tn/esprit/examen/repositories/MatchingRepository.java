package tn.esprit.examen.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.entities.Matching;

public interface MatchingRepository extends JpaRepository<Matching,Long> {
}
