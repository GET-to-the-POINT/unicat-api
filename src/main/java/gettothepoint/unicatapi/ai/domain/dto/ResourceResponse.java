package gettothepoint.unicatapi.ai.domain.dto;

import gettothepoint.unicatapi.artifact.domain.Section;

public record ResourceResponse(String imageUrl, String script) {
    public static ResourceResponse fromEntity(Section section) {
        return new ResourceResponse(
                section.getContentKey(),
                section.getScript()
        );
    }
}
