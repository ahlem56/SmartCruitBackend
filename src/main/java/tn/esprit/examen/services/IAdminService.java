package tn.esprit.examen.services;
import tn.esprit.examen.entities.Admin;

import java.util.List;

public interface IAdminService {
    Admin addAdmin(Admin admin);
    List<Admin> getAllAdmins();
    Admin getAdminById(Long id);
    Admin updateAdmin(Long id, Admin admin);
    void deleteAdmin(Long id);
}