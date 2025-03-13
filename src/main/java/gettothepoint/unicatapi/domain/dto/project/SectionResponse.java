package gettothepoint.unicatapi.domain.dto.project;

import gettothepoint.unicatapi.domain.entity.dashboard.Section;

public record SectionResponse(Long id, String script, Long sortOrder, String uploadImageUrl) {
    public static SectionResponse fromEntity(Section section) {
        return new SectionResponse(section.getId(), section.getScript(), section.getSortOrder(), section.getUploadImageUrl());
    }
}
