package tn.esprit.examen.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.examen.entities.JobOffer;
import tn.esprit.examen.entities.JobStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface JobOfferRepository extends JpaRepository<JobOffer,Long> {
    long countByStatus(JobStatus status);
    long countByPostedDateAfter(LocalDate date);

    // ✅ Return employer fullName, job count, and profile picture
    @Query("SELECT j.employer.fullName, COUNT(j) as jobCount, j.employer.profilePictureUrl " +
            "FROM JobOffer j " +
            "GROUP BY j.employer.fullName, j.employer.profilePictureUrl " +
            "ORDER BY jobCount DESC")
    List<Object[]> findTopEmployers(Pageable pageable);

    // ✅ Return company name, job count, and logo URL
    @Query("SELECT j.company.name, COUNT(j) as jobCount, j.company.logoUrl " +
            "FROM JobOffer j " +
            "GROUP BY j.company.name, j.company.logoUrl " +
            "ORDER BY jobCount DESC")
    List<Object[]> findTopCompanies(Pageable pageable);


    @Query("SELECT COUNT(j) FROM JobOffer j WHERE j.employer.userId = :employerId")
    Long countJobOffersByEmployer(@Param("employerId") Long employerId);


    @Query("SELECT new map(j.title as title, COUNT(a) as applicationsCount) " +
            "FROM Application a JOIN a.jobOffer j " +
            "WHERE j.employer.userId = :employerId " +
            "GROUP BY j.title " +
            "ORDER BY COUNT(a) DESC")
    List<Map<String, Object>> findTopOffersByApplications(@Param("employerId") Long employerId);


    @Query("SELECT COUNT(j) FROM JobOffer j WHERE j.employer.userId = :employerId AND j.status = :status")
    Long countByEmployerAndStatus(@Param("employerId") Long employerId, @Param("status") JobStatus status);

    boolean existsByJobOfferIdAndEmployer_UserId(Long jobOfferId, Long employerId);


    @Query("SELECT j.title FROM JobOffer j WHERE j.jobOfferId = :jobId")
    String findJobTitleById(@Param("jobId") Long jobId);

    @Query("SELECT j.category, COUNT(j) FROM JobOffer j GROUP BY j.category ORDER BY COUNT(j) DESC")
    List<Object[]> findTopCategories();

}


