package tn.esprit.examen.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.examen.entities.Application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ApplicationRepository extends JpaRepository<Application,Long>  {
    List<Application> findByJobOffer_JobOfferId(Long jobOfferId);
    List<Application> findByCandidateUserId(Long candidateId);
    long countByAppliedAtAfter(LocalDateTime date);
    @Query("SELECT a.candidate.fullName, COUNT(a) as appCount, a.candidate.profilePictureUrl " +
            "FROM Application a " +
            "GROUP BY a.candidate.fullName, a.candidate.profilePictureUrl " +
            "ORDER BY appCount DESC")
    List<Object[]> findTopCandidatesByApplications(Pageable pageable);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.jobOffer.employer.userId = :employerId AND a.appliedAt >= :start AND a.appliedAt <= :end")
    Long countByEmployerAndAppliedAtBetween(@Param("employerId") Long employerId,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    @Query("SELECT a.applicationStatus, COUNT(a) FROM Application a " +
            "WHERE a.jobOffer.employer.userId = :employerId " +
            "GROUP BY a.applicationStatus")
    List<Object[]> countApplicationsByStatus(@Param("employerId") Long employerId);


    @Query("SELECT COUNT(a) FROM Application a WHERE a.jobOffer.employer.userId = :employerId")
    Long countByEmployer(@Param("employerId") Long employerId);

    @Query("SELECT AVG(TIMESTAMPDIFF(DAY, a.appliedAt, CURRENT_TIMESTAMP)) " +
            "FROM Application a WHERE a.jobOffer.employer.userId = :employerId AND a.applicationStatus IN (REJECTED, ACCEPTED)")
    Double getAvgProcessingTime(@Param("employerId") Long employerId);

    @Query("SELECT new map(a.candidate.fullName as name, COUNT(a) as count, a.candidate.profilePictureUrl as avatarUrl) " +
            "FROM Application a " +
            "WHERE a.jobOffer.employer.userId = :employerId " +
            "GROUP BY a.candidate.fullName, a.candidate.profilePictureUrl " +
            "ORDER BY COUNT(a) DESC")
    List<Map<String, Object>> findTopCandidatesByApplicationsCount(@Param("employerId") Long employerId);


}
