package gettothepoint.unicatapi.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "project")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String subtitle;
    private String imageUrl;
    private String projectUrl;

    @Column
    private LocalDateTime createdAt;

    @PrePersist
    protected void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public Project(String title, String subtitle, String imageUrl, String projectUrl) {
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
        this.projectUrl = projectUrl;
    }
}
