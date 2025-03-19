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
    private String imageUrl;

    @Setter
    private String alt;

    @Setter
    @Column(columnDefinition = "TEXT")
    private String script;

    @Setter
    private Long sortOrder;

    @Setter
    private String ttsUrl;

    @Setter
    private String videoUrl;

    @ManyToOne
    @JoinColumn
    private Project project;

    @Builder
    public Section(Project project, Long sortOrder) {
        this.project = project;
        this.sortOrder = sortOrder;
    }

}

