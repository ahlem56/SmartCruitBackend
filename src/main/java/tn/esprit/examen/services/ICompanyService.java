package tn.esprit.examen.services;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.entities.Company;

import java.util.List;

public interface ICompanyService {
    public Company addCompanyWithLogo(Company company, MultipartFile logoFile) ;
    public Company updateCompanyWithLogo(Company company, MultipartFile logoFile) ;
    Company getCompanyById(Long id);
    List<Company> getAllCompanies();
    void removeCompany(Long id);
}
