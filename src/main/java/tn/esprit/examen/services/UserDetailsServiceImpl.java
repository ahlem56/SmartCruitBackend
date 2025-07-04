package tn.esprit.examen.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tn.esprit.examen.entities.Admin;
import tn.esprit.examen.entities.Candidate;
import tn.esprit.examen.entities.Employer;
import tn.esprit.examen.repositories.AdminRepository;
import tn.esprit.examen.repositories.CandidateRepository;
import tn.esprit.examen.repositories.EmployerRepository;

import java.util.ArrayList;
import java.util.Optional;

@AllArgsConstructor
@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private CandidateRepository candidateRepository;

    private EmployerRepository employerRepository;
    private final AdminRepository adminRepository;



    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Candidate> candidateOpt = candidateRepository.findByEmail(email);
        if (candidateOpt.isPresent()) {
            Candidate candidate = candidateOpt.get();
            return org.springframework.security.core.userdetails.User
                    .withUsername(candidate.getEmail())
                    .password(candidate.getPassword())
                    .roles("CANDIDATE")
                    .build();
        }

        Optional<Employer> employerOpt = employerRepository.findByEmail(email);
        if (employerOpt.isPresent()) {
            Employer employer = employerOpt.get();
            return org.springframework.security.core.userdetails.User
                    .withUsername(employer.getEmail())
                    .password(employer.getPassword())
                    .roles("EMPLOYER")
                    .build();
        }

        // ✅ Admin support
        Admin admin = adminRepository.findByEmail(email);
        if (admin != null) {
            return org.springframework.security.core.userdetails.User
                    .withUsername(admin.getEmail())
                    .password(admin.getPassword())
                    .roles("ADMIN")
                    .build();
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }


}
