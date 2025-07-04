package tn.esprit.examen.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.entities.Employer;

import java.util.Optional;

public interface EmployerRepository extends JpaRepository<Employer,Long> {
    Optional<Employer> findByEmail(String email);

}
