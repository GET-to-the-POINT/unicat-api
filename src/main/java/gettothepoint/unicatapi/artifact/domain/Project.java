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

@Getter
@NoArgsConstructor
@Entity
public class Project extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private String title;
    @Setter
    private String description;
    @Setter
    private String scriptTone;
    @Setter
    private String imageStyle;

    @ManyToOne
    @JoinColumn
    private Member member;

    @OneToMany(mappedBy = "project")
    private final List<Section> sections = new ArrayList<>();

    @Builder
    public Project(Member member, String scriptTone, String imageStyle, String description, String title) {
        this.member = member;
        this.scriptTone = scriptTone;
        this.imageStyle = imageStyle;
        this.description = description;
        this.title = title;
    }
}
