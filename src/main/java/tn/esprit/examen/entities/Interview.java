package tn.esprit.examen.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interviewId;

    private LocalDateTime proposedDate;

    private LocalDateTime confirmedDate; // Optional, set when confirmed by candidate

    @Enumerated(EnumType.STRING)
    private InterviewStatus status = InterviewStatus.PENDING;

    private String location; // Or link if it's online

    private String notes;

    @ManyToOne
    private Employer employer;

    @ManyToOne
    private Candidate candidate;

    @OneToOne
    private Application application;
}
