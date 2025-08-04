package tn.esprit.examen.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.examen.entities.Interview;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByCandidateUserId(Long candidateId);
    List<Interview> findByEmployerUserId(Long employerId);
    @Query("SELECT COUNT(i) FROM Interview i " +
            "WHERE i.employer.userId = :employerId " +
            "AND i.status = 'CONFIRMED' " +
            "AND i.confirmedDate >= :startDate")
    Long countUpcomingConfirmedInterviews(@Param("employerId") Long employerId,
                                          @Param("startDate") LocalDateTime startDate);

    @Query("SELECT new map(i.candidate.fullName as candidateName, i.confirmedDate as date) " +
            "FROM Interview i " +
            "WHERE i.employer.userId = :employerId " +
            "AND i.status = 'CONFIRMED' " +
            "AND i.confirmedDate >= CURRENT_TIMESTAMP " +
            "ORDER BY i.confirmedDate ASC")
    List<Map<String, Object>> findUpcomingInterviewsDetails(@Param("employerId") Long employerId);

}


