package tn.esprit.examen.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.entities.*;
import tn.esprit.examen.repositories.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
@Slf4j
public class ApplicationService implements IApplicationService {
    private final ApplicationRepository applicationRepository;
    private final CandidateRepository candidateRepository;
    private final JobOfferRepository jobOfferRepository;
    private final CvService cvService;
    private final CvRepository cvRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final EmployerRepository employerRepository;
    private final MatchingRepository matchingRepository;

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
    public Map<String, Object> applyToJob(
            Long candidateId,
            Long jobOfferId,
            MultipartFile cvFile,
            String firstName,
            String lastName,
            String email,
            String phone,
            String coverLetter
    ) throws IOException {

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée"));

        // ✅ Upload CV to Cloudinary
        String uploadedCvUrl = cvService.uploadCvToCloudinary(cvFile);

        // ✅ Create application
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

        // ✅ Create and save CV
        Cv cv = new Cv();
        cv.setCvUrl(uploadedCvUrl);
        cv.setApplication(app); // link to application
        Cv savedCv = cvRepository.save(cv);
        app.setCv(savedCv);

        // ✅ Extract CV text and build AI inputs
        String cvText = extractTextFromPdf(cvFile.getInputStream());
        String jobText = jobOffer.getDescription() + " "
                + String.join(" ", jobOffer.getRequiredSkills()) + " "
                + jobOffer.getEducationLevel().name();

        log.info("📄 Extracted CV: \n" + cvText);
        log.info("📄 Job Text: \n" + jobText);

        Map<String, Object> feedback = getMatchFeedback(
                jobOffer.getDescription(),
                String.join(" ", jobOffer.getRequiredSkills()),
                cvText
        );

        float aiScore = ((Number) feedback.get("confidence")).floatValue();
        List<String> missingSkills = (List<String>) feedback.get("missing_skills");

        // ✅ Save matching
        Matching matching = new Matching();
        matching.setScore(aiScore);
        matching.setJobOffer(jobOffer);
        matching.setFeedback(String.join(", ", missingSkills));
        matching = matchingRepository.save(matching);

        savedCv.setMatching(matching);
        cvRepository.save(savedCv);

        // ✅ Compose full response (for popup)
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("application", applicationRepository.save(app));
        responseMap.put("score", aiScore);
        responseMap.put("missingSkills", missingSkills);
        responseMap.put("suggestedJobs", suggestJobsFromCv(cvFile));

        return responseMap;
    }

    public List<Application> getApplicationsByJobOffer(Long jobOfferId) {
        return applicationRepository.findByJobOffer_JobOfferId(jobOfferId);
    }


    @Transactional
    public Application acceptApplication(Long applicationId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (app.getApplicationStatus() == ApplicationStatus.ACCEPTED) {
            throw new RuntimeException("Application is already accepted");
        }

        JobOffer jobOffer = app.getJobOffer();

        // Update status of the selected application
        app.setApplicationStatus(ApplicationStatus.ACCEPTED);

        // Optional: Reject other applications if only one position is available
        if (jobOffer.getNumberOfOpenPositions() == 1) {
            List<Application> otherApplications = applicationRepository.findByJobOffer_JobOfferId(jobOffer.getJobOfferId());
            for (Application other : otherApplications) {
                if (!other.getApplicationId().equals(app.getApplicationId())) {
                    other.setApplicationStatus(ApplicationStatus.REJECTED);
                    applicationRepository.save(other);
                }
            }

            // Auto-close the job
            jobOffer.setStatus(JobStatus.INACTIVE);
            jobOfferRepository.save(jobOffer);
        }
        Employer employer = employerRepository.findById(jobOffer.getEmployer().getUserId())
                .orElseThrow(() -> new RuntimeException("Employer not found"));

        notificationService.notifyUser(
                app.getCandidate(),
                employer,
                "You’ve been accepted for the job: " + jobOffer.getTitle()
        );



        // TODO: Notify candidate (email/in-app)
        // e.g. notificationService.sendAcceptanceEmail(app.getEmail(), jobOffer.getTitle());
        try {
            emailService.sendApplicationAcceptedEmail(
                    app.getEmail(),
                    jobOffer.getTitle(),
                    jobOffer.getCompany().getName()
            );
        } catch (Exception e) {
            log.error("Failed to send acceptance email to " + app.getEmail(), e);
        }


        return applicationRepository.save(app);
    }


    @Transactional
    public Application rejectApplication(Long applicationId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (app.getApplicationStatus() == ApplicationStatus.REJECTED) {
            throw new RuntimeException("Application is already rejected");
        }

        app.setApplicationStatus(ApplicationStatus.REJECTED);

        // Optional: Notify candidate
        Employer employer = employerRepository.findById(app.getJobOffer().getEmployer().getUserId())
                .orElseThrow(() -> new RuntimeException("Employer not found"));

        notificationService.notifyUser(
                app.getCandidate(),
                employer,
                "Your application for the job: " + app.getJobOffer().getTitle() + " has been rejected."
        );


        try {
            emailService.sendApplicationRejectedEmail(
                    app.getEmail(),
                    app.getJobOffer().getTitle(),
                    app.getJobOffer().getCompany().getName()
            );
        } catch (Exception e) {
            log.error("Failed to send rejection email to " + app.getEmail(), e);
        }

        return applicationRepository.save(app);
    }

    public List<Application> getApplicationsByCandidate(Long candidateId) {
        return applicationRepository.findByCandidateUserId(candidateId);
    }


    public Map<String, Object> getMatchFeedback(String jobDescription, String jobRequirements, String cvText) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:5000/match";

        Map<String, Object> request = new HashMap<>();
        request.put("cv_text", cvText);
        request.put("job_description", jobDescription);
        request.put("job_requirements", jobRequirements);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("⚠️ Error while calling AI API: ", e);
        }

        // Fallback
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("confidence", 0f);
        fallback.put("missing_skills", List.of());
        return fallback;
    }


    public static String extractTextFromPdf(InputStream inputStream) {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    public List<Map<String, Object>> suggestJobsFromCv(MultipartFile cvFile) throws IOException {
        String cvText = extractTextFromPdf(cvFile.getInputStream());

        // Fetch all job offers
        List<JobOffer> jobOffers = jobOfferRepository.findAll();

        List<Map<String, Object>> suggestions = new ArrayList<>();

        for (JobOffer job : jobOffers) {
            String jobDescription = job.getDescription();
            String jobRequirements = String.join(" ", job.getRequiredSkills());

            Map<String, Object> feedback = getMatchFeedback(jobDescription, jobRequirements, cvText);

            float confidence = ((Number) feedback.get("confidence")).floatValue();

            // Only keep basic job info (no scores/skills)
            Map<String, Object> matchResult = new HashMap<>();
            matchResult.put("jobId", job.getJobOfferId());
            matchResult.put("title", job.getTitle());
            matchResult.put("company", job.getCompany().getName());
            matchResult.put("location", job.getJobLocation());
            matchResult.put("logoUrl", job.getCompany().getLogoUrl()); // ✅ required

            // Use score only for ranking
            matchResult.put("score", confidence); // ⬅️ Temporary, will be removed before return

            suggestions.add(matchResult);
        }

        // Sort by score descending (but don’t return it)
        suggestions.sort((a, b) -> Float.compare(
                (float) b.get("score"),
                (float) a.get("score")
        ));

        // Remove score from final response
        for (Map<String, Object> s : suggestions) {
            s.remove("score");
        }

        return suggestions.subList(0, Math.min(3, suggestions.size()));
    }


}
