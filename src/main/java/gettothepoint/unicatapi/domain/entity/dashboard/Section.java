package gettothepoint.unicatapi.domain.entity.dashboard;

import gettothepoint.unicatapi.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class Section extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uploadImageUrl;

    @Lob
    private String script;

    private String ttsUrl;
    private Long order;

    @ManyToOne
    @JoinColumn
    private Project project;

    @Builder
    public Section(String uploadImageUrl, String script, Project project) {
        this.uploadImageUrl = uploadImageUrl;
        this.script = script;
        this.project = project;
    }

}

