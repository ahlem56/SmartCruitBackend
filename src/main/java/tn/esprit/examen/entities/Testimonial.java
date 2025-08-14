package tn.esprit.examen.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Testimonial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank @Size(min = 10, max = 1000)
    @Column(length = 1000, nullable = false)
    private String content;
    @Min(1) @Max(5)
    private int rating = 5;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestimonialStatus status = TestimonialStatus.PENDING;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String moderationNote;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id")
    @JsonIgnore
    private User author;

    public Long getAuthorId() {
        return author != null ? author.getUserId() : null;
    }
    public String getAuthorName() {
        return author != null ? author.getFullName() : null;
    }

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getAuthorProfilePictureUrl() {
        return author != null ? author.getProfilePictureUrl() : null;
    }

}
