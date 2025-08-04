package tn.esprit.examen.entities;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.beans.factory.annotation.Value;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    @Id
    @GeneratedValue
    private Long applicationId;
    private LocalDateTime appliedAt;
    @Enumerated(EnumType.STRING)
    private ApplicationStatus applicationStatus;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    @Column(length = 2000)
    private String coverLetter;


    @ManyToOne
    private Candidate candidate;

    @ManyToOne
    private JobOffer jobOffer;

    @OneToOne
    @JoinColumn(name = "cv_id", referencedColumnName = "cvId")
    private Cv cv;


}
