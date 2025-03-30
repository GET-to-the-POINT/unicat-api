package gettothepoint.unicatapi.domain.entity.dashboard;

import gettothepoint.unicatapi.domain.entity.BaseEntity;
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Setter
    private Long id;

    @Setter
    private Long sortOrder;

    @Column(columnDefinition = "TEXT")
    @Setter private String alt;
    @Column(columnDefinition = "TEXT")
    @Setter private String script;
    @Setter private String voiceModel;

    @Setter private String contentUrl;
    @Setter private String audioUrl;
    @Setter private String videoUrl;
    @Setter private String transitionUrl;

    @ManyToOne
    @JoinColumn
    private Project project;

    @Builder
    public Section(Project project, String voiceModel, Long sortOrder, String alt, String script, String contentUrl, String audioUrl, String videoUrl, String transitionUrl) {
        this.project = project;
        this.voiceModel = voiceModel;
        this.sortOrder = sortOrder;
        this.alt = alt;
        this.script = script;
        this.contentUrl = contentUrl;
        this.audioUrl = audioUrl;
        this.videoUrl = videoUrl;
        this.transitionUrl = transitionUrl;

    }

}

