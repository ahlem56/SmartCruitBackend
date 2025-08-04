package tn.esprit.examen.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEngagementStatsDto {
    private List<UserRankDto> topCandidates;
    private List<UserRankDto> topEmployers;
    private List<CompanyRankDto> topCompanies;
}
