package tn.esprit.examen.services;

import tn.esprit.examen.entities.JobOffer;

import java.util.List;
import java.util.Optional;

public interface IJobOfferService {
    public JobOffer create(JobOffer jobOffer) ;
    public List<JobOffer> getAll() ;
    public Optional<JobOffer> getById(Long id) ;
    public void delete(Long id) ;

    }
