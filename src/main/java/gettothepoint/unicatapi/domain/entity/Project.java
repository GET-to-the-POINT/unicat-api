package gettothepoint.unicatapi.domain.entity;

import gettothepoint.unicatapi.domain.entity.video.UploadVideo;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String subtitle;
    private String thumbnailUrl;
    private String videoUrl;

    @ManyToOne
    @JoinColumn
    private Member member;

    @OneToOne
    @JoinColumn
    private UploadVideo uploadVideo;

    @OneToMany(mappedBy = "project")
    private final List<Section> sections = new ArrayList<>();

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

    @Builder
    public Project(String title, String subtitle, String thumbnailUrl, String videoUrl, Member member) {
        this.title = title;
        this.subtitle = subtitle;
        this.thumbnailUrl = thumbnailUrl;
        this.videoUrl = videoUrl;
        this.member = member;
    }
}
