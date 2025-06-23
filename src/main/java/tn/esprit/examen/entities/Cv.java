package tn.esprit.examen.entities;

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
    private Long CvId;

    private String education;
    private String experience;
    private String languages;
    private String certifications;
    @ElementCollection
    private List<String> extractedSkills;

    @OneToOne
    private Application application;

    @ManyToOne
    private Matching matching;
}
