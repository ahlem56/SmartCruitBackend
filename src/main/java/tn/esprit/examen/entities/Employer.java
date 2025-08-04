package tn.esprit.examen.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Employer extends User{


    private String linkedInUrl;
    private String githubUrl;
    private String industry;

    public Employer(String fullName, String email, String password) {
        setFullName(fullName);
        setEmail(email);
        setPassword(password); // hashed later
        setCreatedAt(LocalDate.now());
    }

    @ManyToOne
    @JsonIgnoreProperties({"employers", "jobOffers"}) // CLEANER than ManagedReference
    private Company company;

    @OneToMany(mappedBy = "employer", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<JobOffer> jobOffers;


}
