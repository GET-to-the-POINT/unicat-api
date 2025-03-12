package gettothepoint.unicatapi.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Getter
@NoArgsConstructor
@Entity
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uploadImageUrl;

    @Lob
    private String script;

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

