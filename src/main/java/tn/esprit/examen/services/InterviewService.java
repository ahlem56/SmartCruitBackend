package tn.esprit.examen.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.examen.entities.*;
import tn.esprit.examen.repositories.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepo;
    private final ApplicationRepository applicationRepo;
    private final EmployerRepository employerRepo;
    private final CandidateRepository candidateRepo;
    private final NotificationService notificationService;

    @Transactional
    public Interview proposeInterview(Long applicationId, Long employerId, LocalDateTime date, String location, String notes) {
        Application app = applicationRepo.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        Employer employer = employerRepo.findById(employerId)
                .orElseThrow(() -> new RuntimeException("Employer not found"));

        Interview interview = new Interview();
        interview.setApplication(app);
        interview.setCandidate(app.getCandidate());
        interview.setEmployer(employer);
        interview.setProposedDate(date);
        interview.setLocation(location);
        interview.setNotes(notes);
        interview.setStatus(InterviewStatus.PENDING);

        Interview saved = interviewRepo.save(interview);

        // ✅ ensure sender is a managed Employer entity
        notificationService.notifyUser(
                app.getCandidate(),
                employer,
                "Interview proposed for job: " + app.getJobOffer().getTitle() + " on " + date
        );

        return saved;
    }

    @Transactional
    public Interview confirmInterview(Long interviewId, LocalDateTime confirmedDate) {
        Interview interview = interviewRepo.findById(interviewId)
                .orElseThrow(() -> new RuntimeException("Interview not found"));

        interview.setConfirmedDate(confirmedDate);
        interview.setStatus(InterviewStatus.CONFIRMED);

        // ✅ ensure sender is managed Candidate entity
        Candidate candidate = candidateRepo.findById(interview.getCandidate().getUserId())
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        notificationService.notifyUser(
                interview.getEmployer(),
                candidate,
                "Candidate confirmed interview on: " + confirmedDate
        );

        return interviewRepo.save(interview);
    }

    public List<Interview> getInterviewsForCandidate(Long candidateId) {
        return interviewRepo.findByCandidateUserId(candidateId);
    }

    public List<Interview> getInterviewsForEmployer(Long employerId) {
        return interviewRepo.findByEmployerUserId(employerId);
    }

    @Transactional
    public void cancelInterview(Long id) {
        Interview interview = interviewRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Interview not found"));

        interview.setStatus(InterviewStatus.CANCELLED);
        interviewRepo.save(interview);

        // ✅ Fetch sender from DB to ensure it's managed
        Employer employer = employerRepo.findById(interview.getEmployer().getUserId())
                .orElseThrow(() -> new RuntimeException("Employer not found"));

        notificationService.notifyUser(
                interview.getCandidate(),
                employer,
                "Your interview scheduled on " + interview.getProposedDate() + " has been cancelled."
        );
    }
}
