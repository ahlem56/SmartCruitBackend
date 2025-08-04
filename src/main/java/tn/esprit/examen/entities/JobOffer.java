package tn.esprit.examen.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobOfferId;

    @NotBlank(message = "Title is mandatory")
    private String title;


    @Enumerated(EnumType.STRING)
    private JobCategory category;


    @NotBlank(message = "Description is mandatory")
    @Column(length = 2000)
    private String description;

    @NotNull(message = "Salary is required")
    @Min(value = 0, message = "Salary must be a positive number")
    private Float salary;

    @NotBlank(message = "Job location is required")
    private String jobLocation;

    @ElementCollection
    private List<String> requiredSkills;

    @ElementCollection
    private List<String> requiredLanguages;

    @ElementCollection
    private List<String> benefits;

    @NotNull(message = "Education level is required")
    @Enumerated(EnumType.STRING)
    private EducationLevel educationLevel;

    @NotNull(message = "Experience level is required")
    @Enumerated(EnumType.STRING)
    private ExperienceLevel experienceLevel;

    @NotNull(message = "Job type is required")
    @Enumerated(EnumType.STRING)
    private JobType jobType;

    @NotNull(message = "Number of open positions is required")
    @Min(value = 1, message = "There must be at least one open position")
    private Integer numberOfOpenPositions;

    private LocalDate postedDate = LocalDate.now();

    @Future(message = "Deadline must be in the future")
    @NotNull(message = "Deadline is required")
    private LocalDate deadline;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"jobOffers", "company", "password", "roles"})
    @NotNull(message = "Employer is required")
    private Employer employer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"jobOffers", "employers"}) // <--- safer and cleaner
    @NotNull(message = "Company is required")
    private Company company;

    @OneToMany(mappedBy = "jobOffer", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Matching> matchings;

    @OneToMany(mappedBy = "jobOffer", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Application> applications;


    @Transient
    @JsonProperty("applicationsCount")
    public int getApplicationsCount() {
        return applications != null ? applications.size() : 0;
    }
}
