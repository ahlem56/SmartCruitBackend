package tn.esprit.examen.services;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.examen.entities.Admin;
import tn.esprit.examen.repositories.AdminRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService implements IAdminService {

    private final AdminRepository adminRepository;

    // Inject PasswordEncoder
    private final PasswordEncoder passwordEncoder;

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
}
