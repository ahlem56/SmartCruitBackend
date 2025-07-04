package tn.esprit.examen.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.entities.*;
import tn.esprit.examen.repositories.ApplicationRepository;
import tn.esprit.examen.repositories.CandidateRepository;
import tn.esprit.examen.repositories.CvRepository;
import tn.esprit.examen.repositories.JobOfferRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class ApplicationService implements IApplicationService {
    private final ApplicationRepository applicationRepository;
    private final CandidateRepository candidateRepository;
    private final JobOfferRepository jobOfferRepository;
    private final CvService cvService;
    private final CvRepository cvRepository;
    public Application create(Application application) {
        application.setAppliedAt(LocalDateTime.now());
        return applicationRepository.save(application);
    }

    public List<Application> getAll() {
        return applicationRepository.findAll();
    }

    public Optional<Application> getById(Long id) {
        return applicationRepository.findById(id);
    }

    public void delete(Long id) {
        applicationRepository.deleteById(id);
    }



    @Override
    @Transactional
    public Application applyToJob(Long candidateId, Long jobOfferId, MultipartFile cvFile,
                                  String firstName, String lastName, String email,
                                  String phone, String coverLetter) throws IOException {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée"));

        // Upload du CV
        String uploadedCvUrl = cvService.uploadCvToCloudinary(cvFile);

        // Créer l'application
        Application app = new Application();
        app.setCandidate(candidate);
        app.setJobOffer(jobOffer);
        app.setAppliedAt(LocalDateTime.now());
        app.setApplicationStatus(ApplicationStatus.SUBMITTED);
        app.setFirstName(firstName);
        app.setLastName(lastName);
        app.setEmail(email);
        app.setPhone(phone);
        app.setCoverLetter(coverLetter);

        // Créer et associer le CV
        Cv cv = new Cv();
        cv.setCvUrl(uploadedCvUrl);
        cv.setApplication(app); // Back-reference

// ✅ Save CV first
        Cv savedCv = cvRepository.save(cv);
        app.setCv(savedCv);     // Then set it in the application

        return applicationRepository.save(app);

    }


    public List<Application> getApplicationsByJobOffer(Long jobOfferId) {
        return applicationRepository.findByJobOffer_JobOfferId(jobOfferId);
    }

}
