package tn.esprit.examen.services;
import tn.esprit.examen.entities.Admin;
import tn.esprit.examen.entities.AdminDashboardStatsDto;
import tn.esprit.examen.entities.CompanyRankDto;
import tn.esprit.examen.entities.UserRankDto;

import java.util.List;
import java.util.Map;

public interface IAdminService {
    Admin addAdmin(Admin admin);
    List<Admin> getAllAdmins();
    Admin getAdminById(Long id);
    Admin updateAdmin(Long id, Admin admin);
    void deleteAdmin(Long id);
    AdminDashboardStatsDto getDashboardStats();
    List<UserRankDto> getTopCandidates(int limit) ;
    List<UserRankDto> getTopEmployers(int limit) ;
    List<CompanyRankDto> getTopCompanies(int limit) ;
    List<Map<String, Object>> getTopMatchesGlobal();
    List<Map<String, Object>> getTopCategories();


    }