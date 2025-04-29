package gettothepoint.unicatapi.artifact.domain.dto;

import gettothepoint.unicatapi.artifact.domain.Section;
import gettothepoint.unicatapi.filestorage.application.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SectionResponseFactory {

    private final FileService fileService;

    public SectionResponse fromEntity(Section section) {
        return SectionResponse.builder()
                .id(section.getId())
                .script(section.getScript())
                .sortOrder(section.getSortOrder())
                .contentUrl(section.getContentKey() != null ? fileService.load(section.getContentKey()).toString() : null)
                .audioUrl(section.getAudioKey() != null ? fileService.load(section.getAudioKey()).toString() : null)
                .voiceModel(section.getVoiceModel())
                .transitionUrl(section.getTransitionKey() != null ? fileService.load(section.getTransitionKey()).toString() : null)
                .build();
    }
}
