package tn.esprit.examen.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "is_read") // Avoid reserved keyword
    private boolean read = false;

    @ManyToOne
    private User recipient;

    @ManyToOne
    @JsonIgnoreProperties({"jobOffers", "company", "applications"})
    private User sender;

}
