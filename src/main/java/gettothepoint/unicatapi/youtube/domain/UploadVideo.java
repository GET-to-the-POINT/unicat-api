package gettothepoint.unicatapi.youtube.domain;

import gettothepoint.unicatapi.common.domain.BaseEntity;
import gettothepoint.unicatapi.artifact.domain.Project;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@Getter
@Entity
public class UploadVideo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String linkId;

    @OneToOne
    private Project project;

    private String channelId;

    @Builder
    public UploadVideo(String linkId, String channelId, Project project) {
        this.linkId = linkId;
        this.project = project;
        this.channelId = channelId;
    }

}