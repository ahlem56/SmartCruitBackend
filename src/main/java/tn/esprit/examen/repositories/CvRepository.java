package tn.esprit.examen.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.entities.Cv;

public interface CvRepository extends JpaRepository<Cv,Long> {
}
