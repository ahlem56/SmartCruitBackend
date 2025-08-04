package tn.esprit.examen.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.examen.entities.Favorite;
import tn.esprit.examen.entities.JobOffer;
import tn.esprit.examen.entities.User;
import tn.esprit.examen.repositories.FavoriteRepository;
import tn.esprit.examen.repositories.JobOfferRepository;
import tn.esprit.examen.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final JobOfferRepository jobOfferRepository;

    public Favorite addFavorite(Long candidateId, Long jobOfferId) {
        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));

        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new IllegalArgumentException("Job offer not found"));

        boolean exists = favoriteRepository.findByCandidateAndJobOffer(candidate, jobOffer).isPresent();
        if (exists) {
            throw new RuntimeException("Already favorited");
        }

        Favorite favorite = Favorite.builder()
                .candidate(candidate)
                .jobOffer(jobOffer)
                .createdAt(LocalDateTime.now())
                .build();

        return favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(Long candidateId, Long jobOfferId) {
        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));

        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new IllegalArgumentException("Job offer not found"));

        favoriteRepository.deleteByCandidateAndJobOffer(candidate, jobOffer);
    }

    public List<Favorite> getFavoritesByCandidate(Long candidateId) {
        return favoriteRepository.findByCandidate_UserId(candidateId);
    }
}
