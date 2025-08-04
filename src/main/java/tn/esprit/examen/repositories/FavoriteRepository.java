package tn.esprit.examen.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.entities.Favorite;
import tn.esprit.examen.entities.User;
import tn.esprit.examen.entities.JobOffer;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByCandidate_UserId(Long candidateId);
    Optional<Favorite> findByCandidateAndJobOffer(User candidate, JobOffer jobOffer);
    void deleteByCandidateAndJobOffer(User candidate, JobOffer jobOffer);
}
