package tn.esprit.examen.services;

import org.springframework.data.domain.Page; // ✅ Spring Page
import tn.esprit.examen.entities.Testimonial;
import tn.esprit.examen.entities.TestimonialStatus;

public interface ITestimonialService {
    Page<Testimonial> listApproved(int page, int size);

    Page<Testimonial> listMine(String bearerToken, int page, int size);

    Testimonial create(String bearerToken, Testimonial incoming);

    Testimonial updateMine(String bearerToken, Long id, Testimonial body);

    void delete(String bearerToken, Long id);

    Page<Testimonial> listAll(TestimonialStatus status, int page, int size);

    Testimonial approve(Long id, String note);

    Testimonial reject(Long id, String note);
}
