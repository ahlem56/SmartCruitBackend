package tn.esprit.examen.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.examen.entities.Company;
import tn.esprit.examen.entities.Employer;
import tn.esprit.examen.repositories.CompanyRepository;
import tn.esprit.examen.repositories.EmployerRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class EmployerService implements IEmployerService {

    private final EmployerRepository employerRepository;
    private final CompanyRepository companyRepository;

    @Override
    public Employer createEmployer(Employer employer, String companyName, String companyWebsite) {
        Company company = new Company();
        company.setName(companyName);
        company.setWebsite(companyWebsite);
        company = companyRepository.save(company);

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
            existing.setContact(updated.getContact());
            existing.setIndustry(updated.getIndustry());
            return employerRepository.save(existing);
        }).orElse(null);
    }

    @Override
    public void deleteEmployer(Long id) {
        employerRepository.deleteById(id);
    }
}
