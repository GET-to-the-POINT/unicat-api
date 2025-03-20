package gettothepoint.unicatapi.domain.dto.project;

import gettothepoint.unicatapi.domain.entity.dashboard.Section;

public record SectionResponse(Long id, String script, Integer imageHashCode, Integer audioHashCode, Integer videoHashCode, Long sortOrder, String uploadImageUrl) {
    public static SectionResponse fromEntity(Section section) {
        return new SectionResponse(section.getId(), section.getScript(), section.getVideoHashCode(), section.getResourceHashCode(), section.getAudioHashCode(), section.getSortOrder(), section.getResourceUrl());
    }
}
