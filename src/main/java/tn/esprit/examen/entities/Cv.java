package tn.esprit.examen.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @ManyToOne
    private Matching matching;
}
