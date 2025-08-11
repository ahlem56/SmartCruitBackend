package tn.esprit.examen.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.entities.Testimonial;
import tn.esprit.examen.entities.TestimonialStatus;

public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {

    Page<Testimonial> findByStatus(TestimonialStatus status, Pageable pageable);

    Page<Testimonial> findByAuthor_UserId(Long userId, Pageable pageable);

    boolean existsByIdAndAuthor_UserId(Long id, Long userId);
}
