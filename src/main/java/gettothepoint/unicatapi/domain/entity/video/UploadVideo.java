package gettothepoint.unicatapi.domain.entity.video;

import gettothepoint.unicatapi.domain.entity.BaseEntity;
import gettothepoint.unicatapi.domain.entity.dashboard.Project;
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

    @Builder
    public UploadVideo(String linkId, Project project) {
        this.linkId = linkId;
        this.project = project;
    }

}