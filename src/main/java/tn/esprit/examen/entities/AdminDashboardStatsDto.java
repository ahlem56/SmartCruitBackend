package tn.esprit.examen.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStatsDto {
    private long totalUsers;
    private long totalJobs;
    private long totalApplications;
    private long activeJobs;

    private long newUsers24h;
    private long newJobs24h;
    private long newApplications24h;

    private long newUsers7d;
    private long newJobs7d;
    private long newApplications7d;

    private long newUsers30d;
    private long newJobs30d;
    private long newApplications30d;

    private long totalCompanies;
    private long totalCandidates;
    private long totalEmployers;

}