package tn.esprit.examen.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.entities.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Admin findByEmail(String email);
}