package tn.esprit.examen.entities;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Company {
    @Id
    @GeneratedValue
    private Long companyId;

    private String name;
    private String website;
    private String logoUrl;
    private String industry;
    private String description;

    private String address;

    private String contactEmail;
    private String contactPhone;


    private String linkedInUrl;
    private String twitterUrl;
    private String facebookUrl;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    @JsonIgnore // Avoid recursion
    private List<Employer> employers;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<JobOffer> jobOffers;



}
