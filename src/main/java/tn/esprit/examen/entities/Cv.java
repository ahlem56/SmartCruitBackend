package tn.esprit.examen.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Cv {
    @Id
    @GeneratedValue
    private Long cvId;
    private String cvUrl;

    private String education;
    private String experience;
    private String languages;
    private String certifications;
    @ElementCollection
    private List<String> extractedSkills;

    @OneToOne
    @JsonIgnore
    private Application application;

    @ManyToOne(fetch = FetchType.EAGER) // <-- force fetch
    @JsonIgnoreProperties({"cvs", "jobOffer"}) // avoid infinite loop
    private Matching matching;

}
