package tn.esprit.examen.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
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
@RequiredArgsConstructor
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
    @Value("${youtube.api.key}")
    private String youtubeApiKey;

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
                .orElseThrow(() -> new RuntimeException("Candidate not found"));
        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new RuntimeException("Job offer not found"));

        String uploadedCvUrl = cvService.uploadCvToCloudinary(cvFile);

        Application application = new Application();
        application.setCandidate(candidate);
        application.setJobOffer(jobOffer);
        application.setAppliedAt(LocalDateTime.now());
        application.setApplicationStatus(ApplicationStatus.SUBMITTED);
        application.setFirstName(firstName);
        application.setLastName(lastName);
        application.setEmail(email);
        application.setPhone(phone);
        application.setCoverLetter(coverLetter);

        Cv cv = new Cv();
        cv.setCvUrl(uploadedCvUrl);
        cv.setApplication(application);
        Cv savedCv = cvRepository.save(cv);
        application.setCv(savedCv);

        String cvText = extractTextFromPdf(cvFile.getInputStream());
        String jobText = jobOffer.getDescription() + " " +
                String.join(" ", jobOffer.getRequiredSkills()) + " " +
                jobOffer.getEducationLevel().name();

        log.info("📄 Extracted CV text:\n{}", cvText);
        log.info("📄 Combined job text:\n{}", jobText);

        Map<String, Object> feedback = getMatchFeedback(
                jobOffer.getDescription(),
                String.join(" ", jobOffer.getRequiredSkills()),
                cvText
        );

        float aiScore = ((Number) feedback.get("confidence")).floatValue();
        List<String> missingSkills = (List<String>) feedback.get("missing_skills");

        Matching matching = new Matching();
        matching.setScore(aiScore);
        matching.setJobOffer(jobOffer);
        matching.setFeedback(String.join(", ", missingSkills));
        matching = matchingRepository.save(matching);

        savedCv.setMatching(matching);
        cvRepository.save(savedCv);

        List<Map<String, String>> suggestedCourses = getFreeCoursesForSkills(missingSkills, youtubeApiKey);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("application", applicationRepository.save(application));
        responseMap.put("score", aiScore);
        responseMap.put("missingSkills", missingSkills);
        responseMap.put("suggestedCourses", suggestedCourses);
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

        app.setApplicationStatus(ApplicationStatus.ACCEPTED);

        if (jobOffer.getNumberOfOpenPositions() == 1) {
            List<Application> otherApplications = applicationRepository.findByJobOffer_JobOfferId(jobOffer.getJobOfferId());
            for (Application other : otherApplications) {
                if (!other.getApplicationId().equals(app.getApplicationId())) {
                    other.setApplicationStatus(ApplicationStatus.REJECTED);
                    applicationRepository.save(other);
                }
            }

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

        List<JobOffer> jobOffers = jobOfferRepository.findAll();

        List<Map<String, Object>> suggestions = new ArrayList<>();

        for (JobOffer job : jobOffers) {
            String jobDescription = job.getDescription();
            String jobRequirements = String.join(" ", job.getRequiredSkills());

            Map<String, Object> feedback = getMatchFeedback(jobDescription, jobRequirements, cvText);

            float confidence = ((Number) feedback.get("confidence")).floatValue();

            Map<String, Object> matchResult = new HashMap<>();
            matchResult.put("jobId", job.getJobOfferId());
            matchResult.put("title", job.getTitle());
            matchResult.put("company", job.getCompany().getName());
            matchResult.put("location", job.getJobLocation());
            matchResult.put("logoUrl", job.getCompany().getLogoUrl()); // ✅ required

            matchResult.put("score", confidence); // ⬅️ Temporary, will be removed before return

            suggestions.add(matchResult);
        }
        suggestions.sort((a, b) -> Float.compare(
                (float) b.get("score"),
                (float) a.get("score")
        ));

        for (Map<String, Object> s : suggestions) {
            s.remove("score");
        }

        return suggestions.subList(0, Math.min(3, suggestions.size()));
    }



    public List<Map<String, String>> getFreeCoursesForSkills(List<String> missingSkills, String apiKey) {
        List<Map<String, String>> courseSuggestions = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();

        for (String skill : missingSkills) {
            String query = "Free " + skill + " course";
            String url = "https://www.googleapis.com/youtube/v3/search"
                    + "?part=snippet"
                    + "&q=" + query.replace(" ", "%20")
                    + "&type=video"
                    + "&maxResults=1"
                    + "&key=" + apiKey;

            try {
                Map response = restTemplate.getForObject(url, Map.class);
                List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");

                if (!items.isEmpty()) {
                    Map<String, Object> video = items.get(0);
                    Map<String, Object> id = (Map<String, Object>) video.get("id");
                    Map<String, Object> snippet = (Map<String, Object>) video.get("snippet");

                    Map<String, String> course = new HashMap<>();
                    course.put("skill", skill);
                    course.put("title", (String) snippet.get("title"));
                    course.put("url", "https://www.youtube.com/watch?v=" + id.get("videoId"));
                    course.put("videoId", (String) id.get("videoId"));  // 👈 This enables thumbnail preview


                    courseSuggestions.add(course);
                }
            } catch (Exception e) {
                System.out.println("Error fetching course for skill: " + skill);
            }
        }

        return courseSuggestions;
    }

}
