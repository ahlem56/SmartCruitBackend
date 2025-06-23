package tn.esprit.examen.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.examen.entities.JobOffer;
import tn.esprit.examen.repositories.JobOfferRepository;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class JobOfferService {

    private JobOfferRepository jobOfferRepository;

    public JobOffer create(JobOffer jobOffer) {
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
