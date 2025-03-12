package gettothepoint.unicatapi.domain.entity.video;

import gettothepoint.unicatapi.domain.entity.Project;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigInteger;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Entity
public class UploadVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String linkId;

    private BigInteger viewCount;
    private BigInteger likeCount;
    private BigInteger commentCount;

    @OneToOne
    private Project project;

    @Builder
    public UploadVideo(String linkId, Project project) {
        this.linkId = linkId;
        this.project = project;
    }

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}