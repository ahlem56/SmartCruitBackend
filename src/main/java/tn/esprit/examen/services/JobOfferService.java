package tn.esprit.examen.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.examen.entities.Company;
import tn.esprit.examen.entities.JobOffer;
import tn.esprit.examen.entities.Employer;
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

    private final JobOfferRepository jobOfferRepository;
    private final CompanyRepository companyRepository;
    private final EmployerRepository employerRepository;

    @Override
    public JobOffer create(JobOffer jobOffer) {
        jobOffer.setPostedDate(LocalDate.now());

        // ✅ Ensure employer is set
        if (jobOffer.getEmployer() == null || jobOffer.getEmployer().getUserId() == null) {
            throw new IllegalArgumentException("Employer is required for job offer creation.");
        }

        // ✅ Fetch managed employer entity with company info
        Employer managedEmployer = employerRepository.findById(jobOffer.getEmployer().getUserId())
                .orElseThrow(() -> new RuntimeException("Employer not found with ID: " + jobOffer.getEmployer().getUserId()));

        // ✅ Set employer and their company automatically
        jobOffer.setEmployer(managedEmployer);

        if (managedEmployer.getCompany() == null) {
            throw new RuntimeException("Employer is not associated with any company.");
        }

        jobOffer.setCompany(managedEmployer.getCompany());

        log.info("Creating job offer: Employer ID {}, Company ID {}, Title '{}'",
                managedEmployer.getUserId(),
                managedEmployer.getCompany().getCompanyId(),
                jobOffer.getTitle());

        return jobOfferRepository.save(jobOffer);
    }

    @Override
    public List<JobOffer> getAll() {
        return jobOfferRepository.findAll();
    }

    @Override
    public Optional<JobOffer> getById(Long id) {
        return jobOfferRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        JobOffer jobOffer = jobOfferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job offer not found with ID: " + id));

        // Break foreign key constraint: CV → Application
        jobOffer.getApplications().forEach(application -> {
            if (application.getCv() != null) {
                application.getCv().setApplication(null);  // break FK from CV
            }
        });

        // Flush changes to make sure FK is broken before deletion
        jobOfferRepository.save(jobOffer);

        // Now it's safe to delete the job offer
        jobOfferRepository.delete(jobOffer);

        log.info("Deleted job offer with ID {}", id);
    }

}
