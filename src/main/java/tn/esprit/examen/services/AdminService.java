package tn.esprit.examen.services;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.examen.entities.*;
import tn.esprit.examen.repositories.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService implements IAdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final JobOfferRepository jobOfferRepository;
    private final CompanyRepository companyRepository;
    private final ApplicationRepository applicationRepository;
    private final MatchingRepository matchingRepository;
    private final PasswordEncoder passwordEncoder;
    private final CandidateRepository candidateRepository;
    private final EmployerRepository employerRepository;

    @Override
    public Admin addAdmin(Admin admin) {
        admin.setCreatedAt(LocalDate.now());
        admin.setPassword(passwordEncoder.encode(admin.getPassword())); // ✅ Hash the password!
        return adminRepository.save(admin);
    }

    @Override
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    @Override
    public Admin getAdminById(Long id) {
        return adminRepository.findById(id).orElse(null);
    }

    @Override
    public Admin updateAdmin(Long id, Admin adminDetails) {
        Admin admin = getAdminById(id);
        if (admin != null) {
            admin.setFullName(adminDetails.getFullName());
            admin.setEmail(adminDetails.getEmail());
            admin.setPassword(adminDetails.getPassword());
            admin.setPhoneNumber(adminDetails.getPhoneNumber());
            return adminRepository.save(admin);
        }
        return null;
    }

    @Override
    public void deleteAdmin(Long id) {
        adminRepository.deleteById(id);
    }


    @Override
    public AdminDashboardStatsDto getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last24h = now.minusHours(24);
        LocalDateTime last7d = now.minusDays(7);
        LocalDateTime last30d = now.minusDays(30);

        return AdminDashboardStatsDto.builder()
                .totalUsers(userRepository.count())
                .totalJobs(jobOfferRepository.count())
                .totalApplications(applicationRepository.count())
                .activeJobs(jobOfferRepository.countByStatus(JobStatus.ACTIVE))

                .newUsers24h(userRepository.countByCreatedAtAfter(last24h.toLocalDate()))
                .newJobs24h(jobOfferRepository.countByPostedDateAfter(last24h.toLocalDate()))
                .newApplications24h(applicationRepository.countByAppliedAtAfter(last24h))

                .newUsers7d(userRepository.countByCreatedAtAfter(last7d.toLocalDate()))
                .newJobs7d(jobOfferRepository.countByPostedDateAfter(last7d.toLocalDate()))
                .newApplications7d(applicationRepository.countByAppliedAtAfter(last7d))

                .newUsers30d(userRepository.countByCreatedAtAfter(last30d.toLocalDate()))
                .newJobs30d(jobOfferRepository.countByPostedDateAfter(last30d.toLocalDate()))
                .newApplications30d(applicationRepository.countByAppliedAtAfter(last30d))

                .totalCompanies(companyRepository.count()) // ✅ added
                .totalCandidates(candidateRepository.count())
                .totalEmployers(employerRepository.count())

                .build();
    }


    public List<UserRankDto> getTopCandidates(int limit) {
        List<Object[]> result = applicationRepository.findTopCandidatesByApplications(PageRequest.of(0, limit));
        return result.stream()
                .map(row -> {
                    String name = (String) row[0];
                    int count = ((Number) row[1]).intValue();
                    String avatarUrl = row.length > 2 ? (String) row[2] : "assets/images/default-avatar.png";
                    return new UserRankDto(name, avatarUrl, count);
                })
                .toList();
    }


    public List<UserRankDto> getTopEmployers(int limit) {
        List<Object[]> result = jobOfferRepository.findTopEmployers(PageRequest.of(0, limit));
        return result.stream()
                .map(row -> {
                    String name = (String) row[0];
                    int count = ((Number) row[1]).intValue();
                    String avatarUrl = row.length > 2 ? (String) row[2] : "assets/images/default-avatar.png";
                    return new UserRankDto(name, avatarUrl, count);
                })
                .toList();
    }


    public List<CompanyRankDto> getTopCompanies(int limit) {
        List<Object[]> result = jobOfferRepository.findTopCompanies(PageRequest.of(0, limit));
        return result.stream()
                .map(row -> {
                    String name = (String) row[0];
                    int count = ((Number) row[1]).intValue();
                    String logoUrl = row.length > 2 ? (String) row[2] : "assets/images/default-company.png";
                    return new CompanyRankDto(name, logoUrl, count);
                })
                .toList();
    }

    public List<Map<String, Object>> getTopMatchesGlobal() {
        return matchingRepository.findTopMatchesGlobal();
    }


    public List<Map<String, Object>> getTopCategories() {
        List<Object[]> result = jobOfferRepository.findTopCategories();
        return result.stream()
                .map(row -> {
                    Map<String, Object> map = Map.of(
                            "category", row[0].toString(),
                            "count", ((Number) row[1]).intValue()
                    );
                    return map;
                })
                .toList();
    }


}
