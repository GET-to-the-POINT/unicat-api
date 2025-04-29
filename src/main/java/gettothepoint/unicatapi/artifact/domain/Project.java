package gettothepoint.unicatapi.artifact.domain;

import gettothepoint.unicatapi.common.domain.BaseEntity;
import gettothepoint.unicatapi.member.domain.Member;
import gettothepoint.unicatapi.youtube.domain.UploadVideo;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@NoArgsConstructor
public class Project extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String title;
    private String subtitle;
    private String description;
    private String thumbnailUrl;
    private String artifactKey;
    private String scriptTone;
    private String imageStyle;
    private String templateKey;
    private String titleImageKey;

    @ManyToOne
    @JoinColumn
    private Member member;

    @OneToOne
    @JoinColumn
    private UploadVideo uploadVideo;

    @OneToMany(mappedBy = "project")
    private final List<Section> sections = new ArrayList<>();

    @Builder
    public Project(Member member, String templateKey, String titleImageKey, String scriptTone, String imageStyle, String description, String title, String subtitle) {
        this.member = member;
        this.templateKey = templateKey;
        this.titleImageKey = titleImageKey;
        this.scriptTone = scriptTone;
        this.imageStyle = imageStyle;
        this.description = description;
        this.title = title;
        this.subtitle = subtitle;
    }

}
