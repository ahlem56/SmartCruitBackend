package tn.esprit.examen.services;

import tn.esprit.examen.entities.Candidate;

import java.util.List;

public interface ICandidateService {
    List<Candidate> getAllCandidates();
    Candidate updateCandidate(Long id, Candidate updatedCandidate);
    void deleteCandidate(Long id);

}
