package tn.esprit.examen.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.entities.User;

import java.time.LocalDate;

public interface UserRepository extends JpaRepository<User,Long> {
    long countByCreatedAtAfter(LocalDate date);

}
