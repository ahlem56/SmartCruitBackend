package tn.esprit.examen.services;

import tn.esprit.examen.entities.JobOffer;

import java.util.List;
import java.util.Optional;

public interface IJobOfferService {
    JobOffer create(JobOffer jobOffer) ;
    List<JobOffer> getAll() ;
    Optional<JobOffer> getById(Long id) ;
    void delete(Long id) ;

    }
