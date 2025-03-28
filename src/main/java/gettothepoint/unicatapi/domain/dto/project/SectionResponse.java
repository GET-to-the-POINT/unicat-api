package gettothepoint.unicatapi.domain.dto.project;

import gettothepoint.unicatapi.domain.entity.dashboard.Section;

public record SectionResponse(Long id, String script, Long sortOrder, String contentUrl, String audioUrl, String videoUrl) {
    public static SectionResponse fromEntity(Section section) {
        return new SectionResponse(section.getId(), section.getScript(), section.getSortOrder(), section.getContentUrl(), section.getAudioUrl(), section.getVideoUrl());
    }
}
