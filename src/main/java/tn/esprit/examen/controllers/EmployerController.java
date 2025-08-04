package tn.esprit.examen.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.entities.Employer;
import tn.esprit.examen.services.EmployerDashboardService;
import tn.esprit.examen.services.IEmployerService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@AllArgsConstructor
@RequestMapping("/employer")
public class EmployerController {

    private final IEmployerService employerService;
    private final PasswordEncoder passwordEncoder;
    private final EmployerDashboardService dashboardService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<?> createEmployer(@RequestBody Map<String, String> body) {
        try {
            Employer employer = new Employer();
            employer.setFullName(body.get("fullName"));
            employer.setEmail(body.get("email"));
            employer.setPhoneNumber(body.get("phoneNumber"));
            employer.setBirthDate(LocalDate.parse(body.get("birthDate")));
            employer.setPassword(passwordEncoder.encode(body.get("password")));
            employer.setLinkedInUrl(body.get("linkedInUrl"));
            employer.setGithubUrl(body.get("githubUrl"));
            employer.setIndustry(body.get("industry"));
            employer.setCreatedAt(LocalDate.now());

            Long companyId = Long.parseLong(body.get("companyId"));

            Employer saved = employerService.createEmployer(employer, companyId);
            return ResponseEntity.ok(saved);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid companyId format");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating employer: " + e.getMessage());
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/getAll")
    public List<Employer> getAllEmployers() {
        return employerService.getAllEmployers();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/get/{id}")
    public ResponseEntity<?> getEmployerById(@PathVariable Long id) {
        return employerService.getEmployerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateEmployer(@PathVariable Long id, @RequestBody Employer updated) {
        Employer result = employerService.updateEmployer(id, updated);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteEmployer(@PathVariable Long id) {
        employerService.deleteEmployer(id);
        return ResponseEntity.ok("Employer deleted");
    }

    @GetMapping("/dashboard/applicationsSummary/{employerId}")
    public ResponseEntity<Map<String, Long>> getApplicationSummary(@PathVariable Long employerId) {
        return ResponseEntity.ok(dashboardService.getNewApplicationsStats(employerId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/dashboard/upcomingInterviews/{employerId}")
    public ResponseEntity<Long> getUpcomingInterviews(@PathVariable Long employerId) {
        Long count = dashboardService.getUpcomingInterviewsCount(employerId);
        return ResponseEntity.ok(count);
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/dashboard/funnel/{employerId}")
    public ResponseEntity<Map<String, Long>> getFunnelData(@PathVariable Long employerId) {
        return ResponseEntity.ok(dashboardService.getFunnelStats(employerId));
    }

    @GetMapping("/dashboard/kpis/{employerId}")
    public ResponseEntity<Map<String, Object>> getKpis(@PathVariable Long employerId) {
        return ResponseEntity.ok(dashboardService.getKpiCards(employerId));
    }

    @GetMapping("/dashboard/topCandidates/{employerId}")
    public ResponseEntity<List<Map<String, Object>>> getTopCandidates(@PathVariable Long employerId) {
        return ResponseEntity.ok(dashboardService.getTopCandidates(employerId));
    }

    @GetMapping("/dashboard/topOffers/{employerId}")
    public ResponseEntity<List<Map<String, Object>>> getTopOffers(@PathVariable Long employerId) {
        return ResponseEntity.ok(dashboardService.getTopJobOffers(employerId));
    }

    @GetMapping("/dashboard/upcomingDetailed/{employerId}")
    public ResponseEntity<List<Map<String, Object>>> getDetailedInterviews(@PathVariable Long employerId) {
        return ResponseEntity.ok(dashboardService.getUpcomingInterviewsDetailed(employerId));
    }


    @GetMapping("/dashboard/applicationTrends/{employerId}")
    public ResponseEntity<Map<String, Long>> getApplicationTrends(@PathVariable Long employerId) {
        return ResponseEntity.ok(dashboardService.getApplicationTrends(employerId));
    }

    @GetMapping("/dashboard/offerStatus/{employerId}")
    public ResponseEntity<Map<String, Long>> getOfferStatus(@PathVariable Long employerId) {
        return ResponseEntity.ok(dashboardService.getOfferStatusCounts(employerId));
    }


    @GetMapping("/dashboard/topMatches/{employerId}")
    public ResponseEntity<List<Map<String, Object>>> getTopMatches(@PathVariable Long employerId) {
        return ResponseEntity.ok(dashboardService.getTopMatches(employerId));
    }

}
