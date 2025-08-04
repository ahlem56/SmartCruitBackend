package tn.esprit.examen.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.examen.entities.Candidate;
import tn.esprit.examen.repositories.CandidateRepository;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class CandidateService implements ICandidateService{
    private final CandidateRepository candidateRepository;

    @Override
    public List<Candidate> getAllCandidates() {
        return candidateRepository.findAll();
    }

    @Override
    public Candidate updateCandidate(Long id, Candidate updatedCandidate) {
        Candidate existing = candidateRepository.findById(id).orElse(null);
        if (existing != null) {
            existing.setFullName(updatedCandidate.getFullName());
            existing.setPhoneNumber(updatedCandidate.getPhoneNumber());
            existing.setPreferredJobTitle(updatedCandidate.getPreferredJobTitle());
            existing.setAddress(updatedCandidate.getAddress());
            existing.setEducationLevel(updatedCandidate.getEducationLevel());
            existing.setCurrentPosition(updatedCandidate.getCurrentPosition());
            existing.setPortfolioUrl(updatedCandidate.getPortfolioUrl());
            existing.setBio(updatedCandidate.getBio());
            existing.setLinkedinUrl(updatedCandidate.getLinkedinUrl());
            existing.setGithubUrl(updatedCandidate.getGithubUrl());
            existing.setProfilePictureUrl(updatedCandidate.getProfilePictureUrl());
            return candidateRepository.save(existing);
        }
        return null;
    }

    @Override
    public void deleteCandidate(Long id) {
        candidateRepository.deleteById(id);
    }

}
