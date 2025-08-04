package tn.esprit.examen.controllers;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.entities.*;
import tn.esprit.examen.repositories.*;
import tn.esprit.examen.services.*;

import java.util.*;

@RestController
@RequestMapping("/api/home")
@AllArgsConstructor
public class HomeController {

    private final JobOfferRepository jobOfferRepo;
    private final CompanyRepository companyRepo;
    private final CandidateRepository candidateRepo;

    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        long jobCount = jobOfferRepo.count();
        long companyCount = companyRepo.count();
        long candidateCount = candidateRepo.count();

        Map<String, Long> stats = new HashMap<>();
        stats.put("jobs", jobCount);
        stats.put("companies", companyCount);
        stats.put("candidates", candidateCount);

        return stats;
    }

    @GetMapping("/top-companies")
    public List<Map<String, Object>> getTopCompanies() {
        List<Object[]> results = jobOfferRepo.findTopCompanies(PageRequest.of(0, 6));

        List<Map<String, Object>> topCompanies = new ArrayList<>();
        for (Object[] row : results) {
            Company company = companyRepo.findByName((String) row[0]);

            Map<String, Object> map = new HashMap<>();
            map.put("name", company.getName());
            map.put("jobCount", row[1]);
            map.put("logoUrl", company.getLogoUrl());
            map.put("industry", company.getIndustry());
            map.put("website", company.getWebsite());
            map.put("contactEmail", company.getContactEmail());
            map.put("address", company.getAddress());

            topCompanies.add(map);
        }
        return topCompanies;
    }


    @GetMapping("/job-categories")
    public List<Map<String, Object>> getJobCategories() {
        List<JobOffer> offers = jobOfferRepo.findAll();
        Map<String, Long> categoryCounts = new HashMap<>();

        for (JobOffer offer : offers) {
            String category = offer.getCategory().name();
            categoryCounts.put(category, categoryCounts.getOrDefault(category, 0L) + 1);
        }

        List<Map<String, Object>> response = new ArrayList<>();
        for (Map.Entry<String, Long> entry : categoryCounts.entrySet()) {
            Map<String, Object> cat = new HashMap<>();
            cat.put("name", entry.getKey());
            cat.put("jobCount", entry.getValue());
            response.add(cat);
        }

        return response;
    }

    @GetMapping("/featured-jobs")
    public List<JobOffer> getFeaturedJobs() {
        return jobOfferRepo.findAll().stream().limit(6).toList();
    }
}
