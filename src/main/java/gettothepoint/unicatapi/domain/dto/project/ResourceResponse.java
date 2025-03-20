package gettothepoint.unicatapi.domain.dto.project;

import gettothepoint.unicatapi.domain.entity.dashboard.Section;

public record ResourceResponse(String imageUrl, String alt, String script) {
    public static ResourceResponse fromEntity(Section section) {
        return new ResourceResponse(
                section.getResourceUrl(),
                section.getAlt(),
                section.getScript()
        );
    }
}
