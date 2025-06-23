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

public class Employer extends User{
    private String companyName;
    private String companyWebsite;
    private String companyLogoUrl;
    private String contact;

    @OneToMany(mappedBy = "employer", cascade = CascadeType.ALL)
    private List<JobOffer> jobOffers;

}
