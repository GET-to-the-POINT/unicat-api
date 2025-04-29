package gettothepoint.unicatapi.domain.entity.project;

import gettothepoint.unicatapi.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 대시보드의 섹션을 나타내는 엔티티입니다.
 */
@Getter
@NoArgsConstructor
@Entity
@Schema(description = "대시보드의 섹션을 나타내는 엔티티입니다.")
public class Section extends BaseEntity {

    /**
     * 섹션의 고유 식별자입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Setter
    @Schema(description = "섹션의 고유 식별자입니다.", example = "1")
    private Long id;

    /**
     * 섹션의 정렬 순서입니다.
     */
    @Setter
    @Schema(description = "섹션의 정렬 순서입니다.", example = "1")
    private Long sortOrder;

    /**
     * 대체 텍스트입니다.
     */
    @Column(columnDefinition = "TEXT")
    @Setter
    @Schema(description = "대체 텍스트입니다.", example = "Sample alt text")
    private String alt;
    /**
     * 스크립트 텍스트입니다.
     */
    @Column(columnDefinition = "TEXT")
    @Setter
    @Schema(description = "스크립트 텍스트입니다.", example = "Sample script text")
    private String script;
    /**
     * 음성 모델입니다.
     */
    @Setter
    @Schema(description = "음성 모델입니다.", example = "model1")
    private String voiceModel;

    /**
     * 콘텐츠 URL입니다.
     */
    @Setter
    @Schema(description = "콘텐츠 URL입니다.", example = "https://unicat.com/content")
    private String contentKey;
    /**
     * 오디오 URL입니다.
     */
    @Setter
    @Schema(description = "오디오 URL입니다.", example = "https://unicat.com/audio")
    private String audioKey;
    /**
     * 비디오 URL입니다.
     */
    @Setter
    @Schema(description = "비디오 URL입니다.", example = "https://unicat.com/video")
    private String frameKey;
    /**
     * 전환 효과 URL입니다.
     */
    @Setter
    @Schema(description = "전환 효과 URL입니다.", example = "https://unicat.com/transition")
    private String transitionKey;

    /**
     * 연관된 프로젝트입니다.
     */
    @ManyToOne
    @JoinColumn
    @Schema(description = "연관된 프로젝트입니다.")
    private Project project;

    /**
     * 섹션 빌더 생성자입니다.
     *
     * @param project 연관된 프로젝트
     * @param voiceModel 음성 모델
     * @param alt 대체 텍스트
     * @param script 스크립트 텍스트
     * @param contentKey 콘텐츠 URL
     * @param audioKey 오디오 URL
     * @param frameKey 비디오 URL
     * @param transitionKey 전환 효과 URL
     */
    @Builder
    public Section(Project project, String voiceModel, String alt, String script, String contentKey, String audioKey, String frameKey, String transitionKey) {
        this.project = project;
        this.voiceModel = voiceModel;
        this.sortOrder = project.getSections().size() + 1L;
        this.alt = alt;
        this.script = script;
        this.contentKey = contentKey;
        this.audioKey = audioKey;
        this.frameKey = frameKey;
        this.transitionKey = transitionKey;
    }

}

