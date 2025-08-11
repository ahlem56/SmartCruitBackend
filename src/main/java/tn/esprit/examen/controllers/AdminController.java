package tn.esprit.examen.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.entities.Admin;
import tn.esprit.examen.entities.AdminDashboardStatsDto;
import tn.esprit.examen.entities.UserEngagementStatsDto;
import tn.esprit.examen.services.AdminService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/add")
    public Admin addAdmin(@RequestBody Admin admin) {
        return adminService.addAdmin(admin);
    }

    @GetMapping("/getAll")
    public List<Admin> getAllAdmins() {
        return adminService.getAllAdmins();
    }

    @GetMapping("/get/{id}")
    public Admin getAdminById(@PathVariable Long id) {
        return adminService.getAdminById(id);
    }

    @PutMapping("/update/{id}")
    public Admin updateAdmin(@PathVariable Long id, @RequestBody Admin admin) {
        return adminService.updateAdmin(id, admin);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
    }

    @GetMapping("/dashboard/overview")
    public AdminDashboardStatsDto getDashboardOverview() {
        return adminService.getDashboardStats();
    }


    @GetMapping("/dashboard/engagement")
    public UserEngagementStatsDto getUserEngagementStats() {
        return new UserEngagementStatsDto(
                adminService.getTopCandidates(5),  // List<UserRankDto>
                adminService.getTopEmployers(5),  // List<UserRankDto>
                adminService.getTopCompanies(5)   // List<CompanyRankDto>
        );
    }


    @GetMapping("/dashboard/topMatchesGlobal")
    public ResponseEntity<List<Map<String, Object>>> getTopMatchesGlobal() {
        return ResponseEntity.ok(adminService.getTopMatchesGlobal());
    }

    @GetMapping("/dashboard/topCategories")
    public ResponseEntity<List<Map<String, Object>>> getTopCategories() {
        return ResponseEntity.ok(adminService.getTopCategories());
    }



}
