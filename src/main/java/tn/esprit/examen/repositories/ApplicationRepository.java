package tn.esprit.examen.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.entities.Application;

public interface ApplicationRepository extends JpaRepository<Application,Long>  {
}
