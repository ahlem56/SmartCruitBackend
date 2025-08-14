package tn.esprit.examen.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class PasswordResetToken {
    @Id
    @GeneratedValue
    private Long id;
    @Column(length = 255)
    private String token;
    private LocalDateTime expiryDate;

    @OneToOne
    private Candidate candidate;
}
