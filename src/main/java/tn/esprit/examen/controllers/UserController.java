package tn.esprit.examen.controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.entities.Candidate;
import tn.esprit.examen.entities.PasswordResetToken;
import tn.esprit.examen.repositories.CandidateRepository;
import tn.esprit.examen.repositories.PasswordResetTokenRepository;
import tn.esprit.examen.services.EmailService;
import tn.esprit.examen.services.GoogleService;
import tn.esprit.examen.services.UserDetailsServiceImpl;
import tn.esprit.examen.springSecurity.JwtUtil;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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


    /**
     * Candidate Signup (only candidates can register)
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerCandidate(@RequestBody Candidate candidate) {
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
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(credentials.get("email"), credentials.get("password"))
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        // Fetch user from DB
        Candidate candidate = candidateRepository.findByEmail(credentials.get("email"))
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create JWT with username, role, and userId
        final String jwt = jwtUtil.generateToken(
                candidate.getEmail(),
                "CANDIDATE",            // hardcoded role, or fetch dynamically if needed
                candidate.getUserId()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("fullName", candidate.getFullName());
        response.put("email", candidate.getEmail());

        return ResponseEntity.ok(response);
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

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(jwt);

        Candidate candidate = candidateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(candidate);
    }


    @PutMapping("/update-profile/{email}")
    public ResponseEntity<?> updateProfile(@PathVariable String email, @RequestBody Map<String, String> updates) {
        Candidate candidate = candidateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        candidate.setCurrentPosition(updates.get("currentPosition"));
        candidate.setPreferredJobTitle(updates.get("preferredJobTitle"));
        candidate.setEducationLevel(updates.get("educationLevel"));

        candidateRepository.save(candidate);
        return ResponseEntity.ok(Collections.singletonMap("message", "Profile updated successfully"));
    }


}
