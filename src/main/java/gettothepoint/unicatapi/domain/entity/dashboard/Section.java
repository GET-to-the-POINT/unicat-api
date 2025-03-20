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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long id;

    @Setter
    private Long sortOrder;

    @Setter private String alt;
    @Column(columnDefinition = "TEXT")
    @Setter private String script;
    private String voiceModel;

    @Setter private String resourceUrl;
    @Setter private Integer resourceHashCode;
    @Setter private String resourceMimeType;

    @Setter private String audioUrl;
    @Setter private Integer audioHashCode;
    @Setter private String audioMimeType;

    @Setter private String videoUrl;
    @Setter private Integer videoHashCode;
    @Setter private String videoMimeType;

    @ManyToOne
    @JoinColumn
    private Project project;

    @Builder
    public Section(Project project, Long sortOrder) {
        this.project = project;
        this.sortOrder = sortOrder;
    }

}

