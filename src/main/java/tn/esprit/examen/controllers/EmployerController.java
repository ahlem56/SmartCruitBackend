package tn.esprit.examen.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.entities.Company;
import tn.esprit.examen.entities.Employer;
import tn.esprit.examen.repositories.CompanyRepository;
import tn.esprit.examen.repositories.EmployerRepository;
import tn.esprit.examen.services.IEmployerService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/employer")
public class EmployerController {

    private final IEmployerService employerService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/create")
    public ResponseEntity<?> createEmployer(@RequestBody Map<String, String> body) {
        Employer employer = new Employer();
        employer.setFullName(body.get("fullName"));
        employer.setEmail(body.get("email"));
        employer.setPassword(passwordEncoder.encode(body.get("password")));
        employer.setContact(body.get("contact"));
        employer.setIndustry(body.get("industry"));

        Employer saved = employerService.createEmployer(
                employer,
                body.get("companyName"),
                body.get("companyWebsite")
        );
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/getAll")
    public List<Employer> getAllEmployers() {
        return employerService.getAllEmployers();
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getEmployerById(@PathVariable Long id) {
        return employerService.getEmployerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateEmployer(@PathVariable Long id, @RequestBody Employer updated) {
        Employer result = employerService.updateEmployer(id, updated);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteEmployer(@PathVariable Long id) {
        employerService.deleteEmployer(id);
        return ResponseEntity.ok("Employer deleted");
    }
}
