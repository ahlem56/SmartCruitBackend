package tn.esprit.examen.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.entities.Interview;
import tn.esprit.examen.services.InterviewService;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PreAuthorize("hasRole('EMPLOYER')")
    @PostMapping("/propose")
    public Interview proposeInterview(
            @RequestParam Long applicationId,
            @RequestParam Long employerId,
            @RequestParam LocalDateTime proposedDate,
            @RequestParam String location,
            @RequestParam(required = false) String notes
    ) {
        return interviewService.proposeInterview(applicationId, employerId, proposedDate, location, notes);
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @PutMapping("/confirm")
    public Interview confirmInterview(
            @RequestParam Long interviewId,
            @RequestParam LocalDateTime confirmedDate
    ) {
        return interviewService.confirmInterview(interviewId, confirmedDate);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/candidate/{candidateId}")
    public List<Interview> getByCandidate(@PathVariable Long candidateId) {
        return interviewService.getInterviewsForCandidate(candidateId);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/employer/{employerId}")
    public List<Interview> getByEmployer(@PathVariable Long employerId) {
        return interviewService.getInterviewsForEmployer(employerId);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/cancel/{id}")
    public void cancelInterview(@PathVariable Long id) {
        interviewService.cancelInterview(id);
    }
}
