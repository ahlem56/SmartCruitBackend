package tn.esprit.examen.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.entities.Testimonial;
import tn.esprit.examen.entities.TestimonialStatus;
import tn.esprit.examen.services.ITestimonialService;

import java.util.Map;

@RestController
@RequestMapping("/testimonials")
@RequiredArgsConstructor
public class TestimonialController {

    private final ITestimonialService testimonialService;

    @GetMapping("/public")
    public Page<Testimonial> listApproved(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return testimonialService.listApproved(page, size);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/mine")
    public Page<Testimonial> listMine(@RequestHeader("Authorization") String token,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return testimonialService.listMine(token, page, size);
    }

    @PreAuthorize("hasAnyRole('CANDIDATE','EMPLOYER')")
    @PostMapping
    public Testimonial create(@RequestHeader("Authorization") String token,
                              @Valid @RequestBody Testimonial incoming) {
        return testimonialService.create(token, incoming);
    }

    @PreAuthorize("hasAnyRole('CANDIDATE','EMPLOYER')")
    @PutMapping("/{id}")
    public Testimonial updateMine(@RequestHeader("Authorization") String token,
                                  @PathVariable Long id,
                                  @Valid @RequestBody Testimonial body) {
        return testimonialService.updateMine(token, id, body);
    }

    @PreAuthorize("hasAnyRole('CANDIDATE','EMPLOYER','ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@RequestHeader("Authorization") String token,
                                    @PathVariable Long id) {
        testimonialService.delete(token, id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    @GetMapping
    public Page<Testimonial> listAll(@RequestParam(required = false) TestimonialStatus status,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        return testimonialService.listAll(status, page, size);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/approve")
    public Testimonial approve(@PathVariable Long id,
                               @RequestBody(required = false) Map<String, String> body) {
        String note = body != null ? body.get("note") : null;
        return testimonialService.approve(id, note);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PatchMapping("/{id}/reject")
    public Testimonial reject(@PathVariable Long id,
                              @RequestBody(required = false) Map<String, String> body) {
        String note = body != null ? body.get("note") : null;
        return testimonialService.reject(id, note);
    }
}
