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

    @ManyToOne
    private JobOffer jobOffer;

    @OneToMany
    private List<Cv> cvs;

}
