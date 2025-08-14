package tn.esprit.examen.entities;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Matching {
    @Id
    @GeneratedValue
    private Long id;
    private Float score;
    @Column(length = 2000)
    private String feedback; // For example: a comma-separated string of missing skills

    @ManyToOne
    private JobOffer jobOffer;

    @OneToMany(mappedBy = "matching")
    private List<Cv> cvs;


}
