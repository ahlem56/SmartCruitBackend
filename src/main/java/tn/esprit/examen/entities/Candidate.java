package tn.esprit.examen.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Candidate extends User{

    private String address;
    private String educationLevel;
    private String currentPosition;
    private String preferredJobTitle;


    // ✅ Ajoute ce constructeur personnalisé
    public Candidate(String email) {
        super();
        setEmail(email);           // méthode héritée de User
        setCreatedAt(LocalDate.now());
        setPassword("");           // pour éviter null, en attendant que l’utilisateur définisse un mot de passe
    }
    @JsonIgnore
    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL)
    private List<Application> applications;


}
