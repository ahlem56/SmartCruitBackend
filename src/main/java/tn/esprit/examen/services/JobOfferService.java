package tn.esprit.examen.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.examen.entities.Company;
import tn.esprit.examen.entities.JobOffer;
import tn.esprit.examen.repositories.CompanyRepository;
import tn.esprit.examen.repositories.EmployerRepository;
import tn.esprit.examen.repositories.JobOfferRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class JobOfferService implements IJobOfferService {

    private JobOfferRepository jobOfferRepository;
    private CompanyRepository companyRepository;
    private EmployerRepository employerRepository;

    public JobOffer create(JobOffer jobOffer) {
        jobOffer.setPostedDate(LocalDate.now());

        // 🔽 Load managed instances of employer and company
        if (jobOffer.getEmployer() != null) {
            jobOffer.setEmployer(
                    employerRepository.findById(jobOffer.getEmployer().getUserId())
                            .orElseThrow(() -> new RuntimeException("Employer not found"))
            );
        }

        if (jobOffer.getCompany() != null && jobOffer.getCompany().getCompanyId() != null) {
            Company company = companyRepository.findById(jobOffer.getCompany().getCompanyId())
                    .orElseThrow(() -> new RuntimeException("Company not found with ID " + jobOffer.getCompany().getCompanyId()));
            jobOffer.setCompany(company);
        }
        System.out.println("Incoming company: " + jobOffer.getCompany());
        System.out.println("Received company: " + jobOffer.getCompany());
        System.out.println("Company ID: " + (jobOffer.getCompany() != null ? jobOffer.getCompany().getCompanyId() : "null"));

        return jobOfferRepository.save(jobOffer);
    }

    public List<JobOffer> getAll() {
        return jobOfferRepository.findAll();
    }

    public Optional<JobOffer> getById(Long id) {
        return jobOfferRepository.findById(id);
    }

    public void delete(Long id) {
        jobOfferRepository.deleteById(id);
    }
}
