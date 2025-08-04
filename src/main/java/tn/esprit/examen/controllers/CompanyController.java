package tn.esprit.examen.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.entities.Company;
import tn.esprit.examen.repositories.CompanyRepository;
import tn.esprit.examen.services.ICompanyService;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/company")
public class CompanyController {

    private final ICompanyService companyService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Company addCompany(
            @RequestPart("company") Company company,
            @RequestPart(value = "logo", required = false) MultipartFile logo
    ) {

        return companyService.addCompanyWithLogo(company, logo);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Company updateCompany(
            @RequestPart("company") Company company,
            @RequestPart(value = "logo", required = false) MultipartFile logo
    ) {
        return companyService.updateCompanyWithLogo(company, logo);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/get/{id}")
    public Company getCompanyById(@PathVariable Long id) {
        return companyService.getCompanyById(id);
    }

    @GetMapping("/getAll")
    public List<Company> getAllCompanies() {
        return companyService.getAllCompanies();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public void removeCompany(@PathVariable Long id) {
        companyService.removeCompany(id);
    }
}
