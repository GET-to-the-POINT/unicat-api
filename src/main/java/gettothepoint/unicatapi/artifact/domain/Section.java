package gettothepoint.unicatapi.artifact.domain;

import gettothepoint.unicatapi.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Entity
public class Section extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long id;

    private String language;

    @Setter
    private Long sortOrder;

    @Setter
    @Column(columnDefinition = "TEXT")
    private String script;

    @Setter
    private String voiceModel;

    @Setter
    private String contentKey;

    @Setter
    private String audioKey;

    @Setter
    private String transitionKey;

    @ManyToOne
    @JoinColumn
    private Project project;

    @Builder
    private Section(Project project, String language, String voiceModel, long maxOrder, String script, String contentKey, String audioKey, String transitionKey) {
        this.project = project;
        this.language = language;
        this.voiceModel = voiceModel;
        this.sortOrder = maxOrder + 1L;
        this.script = script;
        this.contentKey = contentKey;
        this.audioKey = audioKey;
        this.transitionKey = transitionKey;
    }
}
