package tn.esprit.examen.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.examen.entities.*;
import tn.esprit.examen.repositories.AdminRepository;
import tn.esprit.examen.repositories.CandidateRepository;
import tn.esprit.examen.repositories.EmployerRepository;
import tn.esprit.examen.repositories.TestimonialRepository;
import tn.esprit.examen.springSecurity.JwtUtil;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class TestimonialService implements ITestimonialService {

    private final TestimonialRepository testimonialRepository;
    private final CandidateRepository candidateRepository;
    private final EmployerRepository employerRepository;
    private final AdminRepository adminRepository;
    private final JwtUtil jwtUtil;

    // ---- Helpers ----
    private record CurrentUser(Long id, String role, User user) {}

    private CurrentUser resolveCurrentUser(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer "))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");

        String jwt = bearerToken.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(jwt);

        // Prefer role claim if you expose it in JwtUtil; fallback to repo lookup:
        Optional<Candidate> cand = candidateRepository.findByEmail(email);
        if (cand.isPresent()) return new CurrentUser(cand.get().getUserId(), "CANDIDATE", cand.get());

        Optional<Employer> emp = employerRepository.findByEmail(email);
        if (emp.isPresent()) return new CurrentUser(emp.get().getUserId(), "EMPLOYER", emp.get());

        Admin admin = adminRepository.findByEmail(email);
        if (admin != null) return new CurrentUser(admin.getUserId(), "ADMIN", admin);

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
    }

    private Testimonial getOwnedOrThrow(CurrentUser me, Long id) {
        Testimonial t = testimonialRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Testimonial not found"));
        boolean isOwner = t.getAuthor() != null && t.getAuthor().getUserId().equals(me.id());
        boolean isAdmin = "ADMIN".equals(me.role());
        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }
        return t;
    }

    // ---- API ----

    @Override
    @Transactional(readOnly = true)
    public Page<Testimonial> listApproved(int page, int size) {
        return testimonialRepository.findByStatus(TestimonialStatus.APPROVED, PageRequest.of(page, size));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Testimonial> listMine(String bearerToken, int page, int size) {
        var me = resolveCurrentUser(bearerToken);
        return testimonialRepository.findByAuthor_UserId(me.id(), PageRequest.of(page, size));
    }

    @Override
    public Testimonial create(String bearerToken, Testimonial incoming) {
        var me = resolveCurrentUser(bearerToken);

        // Only Candidate/Employer can create
        if (!"CANDIDATE".equals(me.role()) && !"EMPLOYER".equals(me.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only candidates and employers can create testimonials");
        }

        incoming.setId(null);
        incoming.setAuthor(me.user());
        incoming.setStatus(TestimonialStatus.PENDING);
        incoming.setModerationNote(null);

        return testimonialRepository.save(incoming);
    }

    @Override
    public Testimonial updateMine(String bearerToken, Long id, Testimonial body) {
        var me = resolveCurrentUser(bearerToken);

        if (!"CANDIDATE".equals(me.role()) && !"EMPLOYER".equals(me.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only candidates and employers can update testimonials");
        }

        Testimonial t = testimonialRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Testimonial not found"));

        if (!t.getAuthor().getUserId().equals(me.id())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your testimonial");
        }

        t.setContent(body.getContent());
        t.setRating(body.getRating());
        // Optional: reset status after edits
        t.setStatus(TestimonialStatus.PENDING);

        return testimonialRepository.save(t);
    }

    @Override
    public void delete(String bearerToken, Long id) {
        var me = resolveCurrentUser(bearerToken);
        Testimonial t = getOwnedOrThrow(me, id);
        testimonialRepository.delete(t);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Testimonial> listAll(TestimonialStatus status, int page, int size) {
        if (status != null) {
            return testimonialRepository.findByStatus(status, PageRequest.of(page, size));
        }
        return testimonialRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public Testimonial approve(Long id, String note) {
        Testimonial t = testimonialRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Testimonial not found"));
        t.setStatus(TestimonialStatus.APPROVED);
        t.setModerationNote(note);
        return testimonialRepository.save(t);
    }

    @Override
    public Testimonial reject(Long id, String note) {
        Testimonial t = testimonialRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Testimonial not found"));
        t.setStatus(TestimonialStatus.REJECTED);
        t.setModerationNote(note);
        return testimonialRepository.save(t);
    }
}
