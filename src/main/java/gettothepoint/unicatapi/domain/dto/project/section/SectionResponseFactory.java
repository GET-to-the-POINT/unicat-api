package gettothepoint.unicatapi.domain.dto.project.section;

import gettothepoint.unicatapi.application.service.storage.StorageService;
import gettothepoint.unicatapi.domain.entity.project.Section;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SectionResponseFactory {

    private final StorageService storageService;

    public SectionResponse fromEntity(Section section) {
        return SectionResponse.builder()
                .id(section.getId())
                .script(section.getScript())
                .sortOrder(section.getSortOrder())
                .contentUrl(section.getContentKey() != null ? storageService.getUri(section.getContentKey()).toString() : null)
                .audioUrl(section.getAudioKey() != null ? storageService.getUri(section.getAudioKey()).toString() : null)
                .frameUrl(section.getFrameKey() != null ? storageService.getUri(section.getFrameKey()).toString() : null)
                .alt(section.getAlt())
                .voiceModel(section.getVoiceModel())
                .transitionUrl(section.getTransitionKey() != null ? storageService.getUri(section.getTransitionKey()).toString() : null)
                .build();
    }
}
