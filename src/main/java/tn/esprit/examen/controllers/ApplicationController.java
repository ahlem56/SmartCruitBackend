package tn.esprit.examen.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.entities.Application;
import tn.esprit.examen.entities.Matching;
import tn.esprit.examen.services.ApplicationService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@AllArgsConstructor
@RequestMapping("/application")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public ResponseEntity<Application> create(@RequestBody Application application) {
        return ResponseEntity.ok(applicationService.create(application));
    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/getAll")
    public ResponseEntity<List<Application>> getAll() {
        return ResponseEntity.ok(applicationService.getAll());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/get/{id}")
    public ResponseEntity<Application> getById(@PathVariable Long id) {
        return applicationService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        applicationService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/apply")
    public ResponseEntity<Map<String, Object>> applyToJob(
            @RequestParam Long candidateId,
            @RequestParam Long jobOfferId,
            @RequestParam MultipartFile cvFile,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String coverLetter
    ) throws IOException {
        Map<String, Object> result = applicationService.applyToJob(candidateId, jobOfferId, cvFile,
                firstName, lastName, email, phone, coverLetter);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/byJobOffer/{jobOfferId}")
    public ResponseEntity<List<Map<String, Object>>> getApplicationsByJobOffer(@PathVariable Long jobOfferId) {
        List<Application> applications = applicationService.getApplicationsByJobOffer(jobOfferId);

        List<Map<String, Object>> response = applications.stream().map(app -> {
            Map<String, Object> appMap = new HashMap<>();
            appMap.put("applicationId", app.getApplicationId());
            appMap.put("applicationStatus", app.getApplicationStatus());
            appMap.put("firstName", app.getFirstName());
            appMap.put("lastName", app.getLastName());
            appMap.put("email", app.getEmail());
            appMap.put("phone", app.getPhone());
            appMap.put("coverLetter", app.getCoverLetter());

            if (app.getCandidate() != null) {
                appMap.put("profilePicture", app.getCandidate().getProfilePictureUrl());
                appMap.put("userId", app.getCandidate().getUserId());
            }

            if (app.getCv() != null) {
                appMap.put("cvUrl", app.getCv().getCvUrl());
                appMap.put("experience", app.getCv().getExperience());
                appMap.put("extractedSkills", app.getCv().getExtractedSkills());

                // ✅ Include the matching score
                if (app.getCv().getMatching() != null) {
                    Matching match = app.getCv().getMatching();
                    Map<String, Object> matchingData = new HashMap<>();
                    matchingData.put("score", match.getScore());
                    matchingData.put("feedback", match.getFeedback()); // ✅ Add feedback
                    appMap.put("matching", matchingData);
                }

            }

            if (app.getJobOffer() != null) {
                appMap.put("jobOffer", Map.of("title", app.getJobOffer().getTitle()));
            }

            return appMap;
        }).toList();

        return ResponseEntity.ok(response);
    }


    @PreAuthorize("isAuthenticated()")
    @PutMapping("/accept/{applicationId}")
    public ResponseEntity<Application> acceptApplication(@PathVariable Long applicationId) {
        try {
            Application acceptedApp = applicationService.acceptApplication(applicationId);
            return ResponseEntity.ok(acceptedApp);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/reject/{applicationId}")
    public ResponseEntity<Application> rejectApplication(@PathVariable Long applicationId) {
        try {
            Application rejectedApp = applicationService.rejectApplication(applicationId);
            return ResponseEntity.ok(rejectedApp);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/byCandidate/{candidateId}")
    public ResponseEntity<List<Application>> getApplicationsByCandidate(@PathVariable Long candidateId) {
        List<Application> applications = applicationService.getApplicationsByCandidate(candidateId);
        return ResponseEntity.ok(applications);
    }

    @PostMapping("/suggest-jobs")
    public ResponseEntity<List<Map<String, Object>>> suggestJobs(@RequestParam MultipartFile cvFile) {
        try {
            List<Map<String, Object>> suggestions = applicationService.suggestJobsFromCv(cvFile);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }



}
