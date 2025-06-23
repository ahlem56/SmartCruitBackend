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
public class JobOffer {
    @Id
    @GeneratedValue
    private Long jobOfferId;

    private String title;
    private String description;
    private Float salary;
    private String location;
    @ElementCollection
    private List<String> requiredSkills;

    @ManyToOne
    private Employer employer;

    @OneToMany(mappedBy = "jobOffer", cascade = CascadeType.ALL)
    private List<Matching> matchings;

    @OneToMany
    private List<Application> applications;
}
