package tn.esprit.examen.entities;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompanyRankDto {
    private String name;
    private String logoUrl;
    private int count;
}