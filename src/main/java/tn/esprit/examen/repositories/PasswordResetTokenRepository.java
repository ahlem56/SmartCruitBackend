package tn.esprit.examen.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.entities.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
}