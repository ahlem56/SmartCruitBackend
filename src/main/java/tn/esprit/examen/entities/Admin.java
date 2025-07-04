package tn.esprit.examen.entities;

import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Admin extends User {

    private String role = "ADMIN"; // optional for clarity, could also be managed via enum/authority

    public Admin(String fullName, String email, String password) {
        setFullName(fullName);
        setEmail(email);
        setPassword(password);
        setCreatedAt(java.time.LocalDate.now());
    }
}
