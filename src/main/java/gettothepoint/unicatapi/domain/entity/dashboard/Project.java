package gettothepoint.unicatapi.domain.entity.dashboard;

import gettothepoint.unicatapi.domain.entity.BaseEntity;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.video.UploadVideo;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

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
    @Setter
    private String thumbnailUrl;
    @Setter
    private String artifactUrl;

    @Setter
    private String scriptTone;

    private String imageStyle;


    private String templateUrl;
    private String titleUrl;

    @ManyToOne
    @JoinColumn
    private Member member;

    @OneToOne
    @JoinColumn
    private UploadVideo uploadVideo;

    @OneToMany(mappedBy = "project")
    private final List<Section> sections = new ArrayList<>();

    @Builder
    public Project(Member member, String templateUrl, String titleUrl) {
        this.member = member;
        this.templateUrl = templateUrl;
        this.titleUrl = titleUrl;
    }

    public void assignUploadVideo(UploadVideo uploadVideo) {
        this.uploadVideo = uploadVideo;
    }
}
