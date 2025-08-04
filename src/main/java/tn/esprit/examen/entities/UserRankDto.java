package tn.esprit.examen.entities;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRankDto {
    private String name;
    private String avatarUrl;
    private int count;
}
