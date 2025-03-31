package gettothepoint.unicatapi.domain.dto.project.section;

import gettothepoint.unicatapi.domain.entity.project.Section;
import io.swagger.v3.oas.annotations.media.Schema;

public record SectionResponse(
        @Schema(description = "섹션의 고유 ID입니다.", example = "1")
        Long id,

        @Schema(description = "스크립트 텍스트입니다.", example = "스크립트 예시입니다.")
        String script,

        @Schema(description = "섹션의 정렬 순서입니다.", example = "1")
        Long sortOrder,

        @Schema(description = "콘텐츠 URL입니다.", example = "https://example.com/content")
        String contentUrl,

        @Schema(description = "오디오 URL입니다.", example = "https://example.com/audio")
        String audioUrl,

        @Schema(description = "비디오 URL입니다.", example = "https://example.com/video")
        String videoUrl,

        @Schema(description = "대체 텍스트입니다.", example = "대체 텍스트입니다.")
        String alt,

        @Schema(description = "음성 모델입니다.", example = "model1")
        String voiceModel,

        @Schema(description = "전환 효과 URL입니다.", example = "https://example.com/transition")
        String transitionUrl
) {
    /**
     * Section 엔티티로부터 SectionResponse DTO를 생성합니다.
     *
     * @param section 변환할 섹션 엔티티
     * @return SectionResponse DTO
     */
    public static SectionResponse fromEntity(Section section) {
        return new SectionResponse(
                section.getId(),
                section.getScript(),
                section.getSortOrder(),
                section.getContentKey(),
                section.getAudioKey(),
                section.getFrameKey(),
                section.getAlt(),
                section.getVoiceModel(),
                section.getTransitionKey()
        );
    }
}