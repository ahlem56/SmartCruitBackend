package tn.esprit.examen.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.entities.Company;
import tn.esprit.examen.repositories.CompanyRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class CompanyService implements ICompanyService {
    private final CompanyRepository companyRepository;

    public Company addCompanyWithLogo(Company company, MultipartFile logoFile) {
        try {
            if (logoFile != null && !logoFile.isEmpty()) {
                String logoUrl = uploadLogoToCloudinary(logoFile);
                company.setLogoUrl(logoUrl);
            }
        } catch (IOException e) {
            log.error("Erreur lors de l'upload du logo", e);
            throw new RuntimeException("Erreur lors de l'upload du logo", e);
        }

        return companyRepository.save(company);
    }

    public Company updateCompanyWithLogo(Company company, MultipartFile logoFile) {
        try {
            if (logoFile != null && !logoFile.isEmpty()) {
                String logoUrl = uploadLogoToCloudinary(logoFile);
                company.setLogoUrl(logoUrl);
            }
        } catch (IOException e) {
            log.error("Erreur lors de l'upload du logo", e);
            throw new RuntimeException("Erreur lors de l'upload du logo", e);
        }

        return companyRepository.save(company);
    }


    @Override
    public Company getCompanyById(Long id) {
        return companyRepository.findById(id).orElse(null);
    }

    @Override
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    @Override
    public void removeCompany(Long id) {
        companyRepository.deleteById(id);
    }


    private final Cloudinary cloudinary;

    public String uploadLogoToCloudinary(MultipartFile logoFile) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(logoFile.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "image",
                        "folder", "company_logos"  // Dossier dans Cloudinary
                ));
        return (String) uploadResult.get("secure_url");  // URL finale du logo
    }
}
