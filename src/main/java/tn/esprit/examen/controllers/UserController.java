package tn.esprit.examen.controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.entities.Admin;
import tn.esprit.examen.entities.Candidate;
import tn.esprit.examen.entities.Employer;
import tn.esprit.examen.entities.PasswordResetToken;
import tn.esprit.examen.repositories.AdminRepository;
import tn.esprit.examen.repositories.CandidateRepository;
import tn.esprit.examen.repositories.EmployerRepository;
import tn.esprit.examen.repositories.PasswordResetTokenRepository;
import tn.esprit.examen.services.EmailService;
import tn.esprit.examen.services.GoogleService;
import tn.esprit.examen.services.UserDetailsServiceImpl;
import tn.esprit.examen.springSecurity.JwtUtil;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {

    private CandidateRepository candidateRepository;

    private PasswordEncoder passwordEncoder;

    private JwtUtil jwtUtil;

    private AuthenticationManager authenticationManager;

    private UserDetailsServiceImpl userDetailsService;

    private PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final GoogleService googleService;
    private final EmployerRepository employerRepository;
    private final AdminRepository adminRepository;



    /**
     * Candidate Signup (only candidates can register)
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerCandidate(@Valid @RequestBody Candidate candidate, BindingResult result) {
        if (result.hasErrors()) {
            // Extract and return error messages
            List<String> errors = result.getFieldErrors().stream()
                    .map(e -> e.getField() + ": " + e.getDefaultMessage())
                    .toList();
            return ResponseEntity.badRequest().body(errors);
        }

        if (candidateRepository.findByEmail(candidate.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is already registered.");
        }

        candidate.setPassword(passwordEncoder.encode(candidate.getPassword()));
        candidate.setCreatedAt(LocalDate.now());

        return ResponseEntity.ok(candidateRepository.save(candidate));
    }
    /**
     * User Login (Candidate authentication)
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        // Candidate
        Optional<Candidate> candidateOpt = candidateRepository.findByEmail(email);
        if (candidateOpt.isPresent()) {
            Candidate candidate = candidateOpt.get();
            String jwt = jwtUtil.generateToken(candidate.getEmail(), "CANDIDATE", candidate.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("fullName", candidate.getFullName());
            response.put("email", candidate.getEmail());
            response.put("role", "CANDIDATE");
            response.put("userId", candidate.getUserId());
            response.put("profilePictureUrl", candidate.getProfilePictureUrl()); // ✅ Add this line
            return ResponseEntity.ok(response);
        }

        // Employer
        Optional<Employer> employerOpt = employerRepository.findByEmail(email);
        if (employerOpt.isPresent()) {
            Employer employer = employerOpt.get();
            String jwt = jwtUtil.generateToken(employer.getEmail(), "EMPLOYER", employer.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("fullName", employer.getFullName());
            response.put("email", employer.getEmail());
            response.put("role", "EMPLOYER");
            response.put("userId", employer.getUserId());
            response.put("profilePictureUrl", employer.getProfilePictureUrl()); // ✅ Add this line

            return ResponseEntity.ok(response);
        }

        // ✅ Admin
        Admin admin = adminRepository.findByEmail(email);
        if (admin != null) {
            String jwt = jwtUtil.generateToken(admin.getEmail(), "ADMIN", admin.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("fullName", admin.getFullName());
            response.put("email", admin.getEmail());
            response.put("role", "ADMIN");
            response.put("userId", admin.getUserId());
            response.put("profilePictureUrl", admin.getProfilePictureUrl()); // ✅ Add this line

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        Candidate candidate = candidateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        String token = UUID.randomUUID().toString(); // on enregistrera le token dans l'étape suivante

        String resetLink = "http://localhost:4200/reset-password?token=" + token;
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));
        resetToken.setCandidate(candidate);

        passwordResetTokenRepository.save(resetToken);

        emailService.sendResetLink(email, resetLink);

        return ResponseEntity.ok("Reset link sent.");
    }


    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        String newPassword = payload.get("newPassword");

        System.out.println("TOKEN reçu : " + token);
        System.out.println("NOUVEAU MDP : " + newPassword);
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElse(null);

        if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token.");
        }

        Candidate candidate = resetToken.getCandidate();
        candidate.setPassword(passwordEncoder.encode(newPassword));
        candidateRepository.save(candidate);

        passwordResetTokenRepository.delete(resetToken); // cleanup

        return ResponseEntity.ok(Collections.singletonMap("message", "Password has been reset successfully."));
    }

    @PostMapping("/google-login")
    public ResponseEntity<?> loginWithGoogle(@RequestBody Map<String, String> body) {
        String idToken = body.get("idToken"); // ✅ correct !

        try {
            // Appel de l’API Google pour vérifier le token
            GoogleIdToken.Payload payload = googleService.verifyAccessToken(idToken);

            String email = payload.getEmail();
            Candidate candidate = candidateRepository.findByEmail(email)
                    .orElseGet(() -> candidateRepository.save(new Candidate(email))); // ou créer un user

            String jwt = jwtUtil.generateToken(email, "CANDIDATE", candidate.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("fullName", candidate.getFullName());
            response.put("email", candidate.getEmail());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Google token");
        }
    }
    @PostMapping("/facebook-login")
    public ResponseEntity<?> loginWithFacebook(@RequestBody Map<String, String> body) {
        String authToken = body.get("authToken");

        try {
            // 1. Call Facebook Graph API
            String fbUrl = "https://graph.facebook.com/me?fields=id,name,email&access_token=" + authToken;
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> fbResponse = restTemplate.getForObject(fbUrl, Map.class);

            if (fbResponse == null || !fbResponse.containsKey("email")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Facebook token invalid or missing email");
            }

            String email = fbResponse.get("email").toString();

            // 2. Check or create user
            Candidate candidate = candidateRepository.findByEmail(email)
                    .orElseGet(() -> {
                        Candidate newCandidate = new Candidate();
                        newCandidate.setEmail(email);
                        newCandidate.setCreatedAt(LocalDate.now());
                        return candidateRepository.save(newCandidate);
                    });

            // 3. Generate JWT
            String jwt = jwtUtil.generateToken(email, "CANDIDATE", candidate.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("fullName", candidate.getFullName());
            response.put("email", candidate.getEmail());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Facebook token");
        }
    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(jwt);

        // Candidate
        Optional<Candidate> candidateOpt = candidateRepository.findByEmail(email);
        if (candidateOpt.isPresent()) {
            return ResponseEntity.ok(candidateOpt.get());
        }

        // Employer
        Optional<Employer> employerOpt = employerRepository.findByEmail(email);
        if (employerOpt.isPresent()) {
            return ResponseEntity.ok(employerOpt.get());
        }

        // ✅ Admin
        Optional<Admin> adminOpt = Optional.ofNullable(adminRepository.findByEmail(email));
        if (adminOpt.isPresent()) {
            return ResponseEntity.ok(adminOpt.get());
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }


    @PreAuthorize("isAuthenticated()")
    @PutMapping("/update-profile/{email}")
    public ResponseEntity<?> updateProfile(@PathVariable String email, @RequestBody Map<String, String> updates) {
        // Try Candidate
        Optional<Candidate> candidateOpt = candidateRepository.findByEmail(email);
        if (candidateOpt.isPresent()) {
            Candidate candidate = candidateOpt.get();

            Optional.ofNullable(updates.get("currentPosition")).ifPresent(candidate::setCurrentPosition);
            Optional.ofNullable(updates.get("preferredJobTitle")).ifPresent(candidate::setPreferredJobTitle);
            Optional.ofNullable(updates.get("educationLevel")).ifPresent(candidate::setEducationLevel);
            Optional.ofNullable(updates.get("bio")).ifPresent(candidate::setBio);
            Optional.ofNullable(updates.get("linkedinUrl")).ifPresent(candidate::setLinkedinUrl);
            Optional.ofNullable(updates.get("githubUrl")).ifPresent(candidate::setGithubUrl);
            Optional.ofNullable(updates.get("portfolioUrl")).ifPresent(candidate::setPortfolioUrl);
            Optional.ofNullable(updates.get("fullName")).ifPresent(candidate::setFullName); // for Candidate

            candidateRepository.save(candidate);
            return ResponseEntity.ok(Map.of("message", "Candidate profile updated successfully"));
        }

        // Try Employer
        Optional<Employer> employerOpt = employerRepository.findByEmail(email);
        if (employerOpt.isPresent()) {
            Employer employer = employerOpt.get();


            Optional.ofNullable(updates.get("industry")).ifPresent(employer::setIndustry);
            Optional.ofNullable(updates.get("linkedInUrl")).ifPresent(employer::setLinkedInUrl);
            Optional.ofNullable(updates.get("githubUrl")).ifPresent(employer::setGithubUrl);
            Optional.ofNullable(updates.get("fullName")).ifPresent(employer::setFullName); // for Employer

            employerRepository.save(employer);
            return ResponseEntity.ok(Map.of("message", "Employer profile updated successfully"));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }



    @PutMapping("/upload-profile-picture/{email}")
    public ResponseEntity<?> uploadProfilePicture(@PathVariable String email, @RequestParam("file") MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            String base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);

            // Try Candidate
            Optional<Candidate> candidateOpt = candidateRepository.findByEmail(email);
            if (candidateOpt.isPresent()) {
                Candidate candidate = candidateOpt.get();
                candidate.setProfilePictureUrl(base64Image);
                candidateRepository.save(candidate);
                return ResponseEntity.ok(candidate); // ✅ return full candidate object
            }

            // Try Employer
            Optional<Employer> employerOpt = employerRepository.findByEmail(email);
            if (employerOpt.isPresent()) {
                Employer employer = employerOpt.get();
                employer.setProfilePictureUrl(base64Image);
                employerRepository.save(employer);
                return ResponseEntity.ok(employer); // ✅ return full employer object
            }

            // Try Admin
            Optional<Admin> adminOpt = Optional.ofNullable(adminRepository.findByEmail(email));
            if (adminOpt.isPresent()) {
                Admin admin = adminOpt.get();
                admin.setProfilePictureUrl(base64Image);
                adminRepository.save(admin);
                return ResponseEntity.ok(admin); // ✅ return full admin object
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image.");
        }
    }


    @PutMapping("/update-credentials/{email}")
    public ResponseEntity<?> updateCredentials(@PathVariable String email, @RequestBody Map<String, String> updates) {
        Candidate candidate = candidateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        if (updates.containsKey("email")) {
            candidate.setEmail(updates.get("email"));
        }
        if (updates.containsKey("password")) {
            String newPassword = updates.get("password");
            if (newPassword != null && newPassword.length() >= 6) {
                candidate.setPassword(passwordEncoder.encode(newPassword));
            }
        }

        candidateRepository.save(candidate);
        return ResponseEntity.ok(Map.of("message", "Credentials updated successfully"));
    }


    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        // Try candidate first
        Optional<Candidate> candidateOpt = candidateRepository.findById(id);
        if (candidateOpt.isPresent()) {
            return ResponseEntity.ok(candidateOpt.get());
        }

        // Try employer
        Optional<Employer> employerOpt = employerRepository.findById(id);
        if (employerOpt.isPresent()) {
            return ResponseEntity.ok(employerOpt.get());
        }

        // Try admin
        Optional<Admin> adminOpt = Optional.ofNullable(adminRepository.findById(id).orElse(null));
        if (adminOpt.isPresent()) {
            return ResponseEntity.ok(adminOpt.get());
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }

}
