package tn.esprit.examen.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.examen.entities.Matching;

import java.util.List;
import java.util.Map;

public interface MatchingRepository extends JpaRepository<Matching,Long> {
    @Query("SELECT COUNT(m) FROM Matching m WHERE m.jobOffer.employer.userId = :employerId AND m.score > 0.7")
    Long countHighMatching(@Param("employerId") Long employerId);

    @Query("SELECT AVG(DATEDIFF(a.appliedAt, j.postedDate)) FROM Application a " +
            "JOIN Matching m ON m.jobOffer.jobOfferId = a.jobOffer.jobOfferId " +
            "JOIN JobOffer j ON j.jobOfferId = a.jobOffer.jobOfferId " +
            "WHERE m.score > 0.8 AND j.employer.userId = :employerId")
    Double avgDaysToGoodProfile(@Param("employerId") Long employerId);

    @Query("SELECT COUNT(m) FROM Matching m WHERE m.jobOffer.employer.userId = :employerId AND m.score < 0.3")
    Long countRejectedByScore(@Param("employerId") Long employerId);


    @Query("""
SELECT new map(
    c.fullName as candidateName,
    j.title as jobTitle,
    m.score as score
)
FROM Matching m
JOIN m.cvs cv
LEFT JOIN cv.application a
LEFT JOIN a.candidate c
JOIN m.jobOffer j
WHERE j.employer.userId = :employerId AND c.fullName IS NOT NULL
ORDER BY m.score DESC
""")

    List<Map<String, Object>> findTopMatchesForEmployer(@Param("employerId") Long employerId);


}
