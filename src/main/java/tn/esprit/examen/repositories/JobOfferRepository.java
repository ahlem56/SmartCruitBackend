package tn.esprit.examen.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.entities.JobOffer;

public interface JobOfferRepository extends JpaRepository<JobOffer,Long> {
}
