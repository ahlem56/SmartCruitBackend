package tn.esprit.examen.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.entities.Application;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application,Long>  {
    List<Application> findByJobOffer_JobOfferId(Long jobOfferId);

}
