package tn.esprit.examen.services;

import tn.esprit.examen.entities.Employer;

import java.util.List;
import java.util.Optional;

public interface IEmployerService {
    Employer createEmployer(Employer employer, Long companyId);
    List<Employer> getAllEmployers();
    Optional<Employer> getEmployerById(Long id);
    Employer updateEmployer(Long id, Employer updated);
    void deleteEmployer(Long id);
}
