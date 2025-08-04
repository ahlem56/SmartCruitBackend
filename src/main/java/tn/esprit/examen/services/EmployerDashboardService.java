package tn.esprit.examen.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.examen.entities.ApplicationStatus;
import tn.esprit.examen.entities.Candidate;
import tn.esprit.examen.entities.JobStatus;
import tn.esprit.examen.repositories.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class EmployerDashboardService {


    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final MatchingRepository matchingRepository;
    private final JobOfferRepository jobOfferRepository;


    public Map<String, Long> getNewApplicationsStats(Long employerId) {
        Map<String, Long> stats = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();

        // Today
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        stats.put("today", applicationRepository.countByEmployerAndAppliedAtBetween(employerId, startOfDay, now));

        // This Week (Monday to now)
        LocalDateTime startOfWeek = now.with(java.time.DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
        stats.put("thisWeek", applicationRepository.countByEmployerAndAppliedAtBetween(employerId, startOfWeek, now));

        // This Month (1st of month to now)
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        stats.put("thisMonth", applicationRepository.countByEmployerAndAppliedAtBetween(employerId, startOfMonth, now));

        return stats;
    }

    public Long getUpcomingInterviewsCount(Long employerId) {
        LocalDateTime now = LocalDateTime.now();
        return interviewRepository.countUpcomingConfirmedInterviews(employerId, now);
    }

    public Map<String, Long> getFunnelStats(Long employerId) {
        List<Object[]> results = applicationRepository.countApplicationsByStatus(employerId);

        Map<String, Long> funnel = new LinkedHashMap<>();
        // Initialize all possible steps to 0 (to prevent missing keys)
        for (ApplicationStatus status : ApplicationStatus.values()) {
            funnel.put(status.name(), 0L);
        }

        for (Object[] row : results) {
            ApplicationStatus status = (ApplicationStatus) row[0];
            Long count = (Long) row[1];
            funnel.put(status.name(), count);
        }

        return funnel;
    }


    public Map<String, Object> getKpiCards(Long employerId) {
        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("totalApplications", applicationRepository.countByEmployer(employerId));
        kpis.put("highMatches", matchingRepository.countHighMatching(employerId));
        kpis.put("totalJobOffers", jobOfferRepository.countJobOffersByEmployer(employerId));
        kpis.put("avgProcessingTime", applicationRepository.getAvgProcessingTime(employerId));
        kpis.put("autoRejectRate", matchingRepository.countRejectedByScore(employerId));
        return kpis;
    }


    public List<Map<String, Object>> getTopCandidates(Long employerId) {
        return applicationRepository.findTopCandidatesByApplicationsCount(employerId);
    }
    public List<Map<String, Object>> getTopJobOffers(Long employerId) {
        return jobOfferRepository.findTopOffersByApplications(employerId);
    }


    public List<Map<String, Object>> getUpcomingInterviewsDetailed(Long employerId) {
        return interviewRepository.findUpcomingInterviewsDetails(employerId);
    }



    public Map<String, Long> getApplicationTrends(Long employerId) {
        Map<String, Long> trends = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 3; i >= 0; i--) {
            LocalDateTime start = now.minusWeeks(i + 1).with(java.time.DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
            LocalDateTime end = now.minusWeeks(i).with(java.time.DayOfWeek.MONDAY).toLocalDate().atStartOfDay();

            String label = "Week " + (4 - i);
            Long count = applicationRepository.countByEmployerAndAppliedAtBetween(employerId, start, end);
            trends.put(label, count);
        }

        return trends;
    }


    public Map<String, Long> getOfferStatusCounts(Long employerId) {
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        statusCounts.put("ACTIVE", jobOfferRepository.countByEmployerAndStatus(employerId, JobStatus.ACTIVE));
        statusCounts.put("DRAFT", jobOfferRepository.countByEmployerAndStatus(employerId, JobStatus.DRAFT));
        statusCounts.put("INACTIVE", jobOfferRepository.countByEmployerAndStatus(employerId, JobStatus.INACTIVE));
        return statusCounts;
    }

    public List<Map<String, Object>> getTopMatches(Long employerId) {
        return matchingRepository.findTopMatchesForEmployer(employerId);
    }


}
