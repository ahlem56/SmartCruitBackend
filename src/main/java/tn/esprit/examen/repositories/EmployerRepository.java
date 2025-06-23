package tn.esprit.examen.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.entities.Employer;

public interface EmployerRepository extends JpaRepository<Employer,Long> {
}
