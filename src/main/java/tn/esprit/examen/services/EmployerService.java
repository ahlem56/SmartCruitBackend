package tn.esprit.examen.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.examen.entities.Company;
import tn.esprit.examen.entities.Employer;
import tn.esprit.examen.repositories.CompanyRepository;
import tn.esprit.examen.repositories.EmployerRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class EmployerService implements IEmployerService {

    private final EmployerRepository employerRepository;
    private final CompanyRepository companyRepository;

    /**
     * Link employer to an existing company (by ID)
     */
    @Override
    public Employer createEmployer(Employer employer, Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        employer.setCompany(company);
        employer.setCreatedAt(LocalDate.now());

        return employerRepository.save(employer);
    }

    @Override
    public List<Employer> getAllEmployers() {
        return employerRepository.findAll();
    }

    @Override
    public Optional<Employer> getEmployerById(Long id) {
        return employerRepository.findById(id);
    }

    @Override
    public Employer updateEmployer(Long id, Employer updated) {
        return employerRepository.findById(id).map(existing -> {
            existing.setFullName(updated.getFullName());
            existing.setEmail(updated.getEmail());
            existing.setPhoneNumber(updated.getPhoneNumber());
            existing.setIndustry(updated.getIndustry());
            existing.setLinkedInUrl(updated.getLinkedInUrl());
            existing.setGithubUrl(updated.getGithubUrl());
            return employerRepository.save(existing);
        }).orElse(null);
    }

    @Override
    public void deleteEmployer(Long id) {
        Optional<Employer> optionalEmployer = employerRepository.findById(id);

        if (optionalEmployer.isEmpty()) {
            log.warn("Tried to delete non-existent employer ID: {}", id);
            return; // or throw custom 404
        }

        Employer employer = optionalEmployer.get();

        // Nullify job offer relationships to avoid FK violations
        employer.getJobOffers().forEach(offer -> {
            offer.setEmployer(null);
        });

        employerRepository.save(employer);
        employerRepository.delete(employer);

        log.info("Successfully deleted employer with ID: {}", id);
    }

}
