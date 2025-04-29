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
                .contentUrl(section.getContentKey() != null ? fileService.downloadFile(section.getContentKey()).toString() : null)
                .audioUrl(section.getAudioKey() != null ? fileService.downloadFile(section.getAudioKey()).toString() : null)
                .voiceModel(section.getVoiceModel())
                .transitionUrl(section.getTransitionKey() != null ? fileService.downloadFile(section.getTransitionKey()).toString() : null)
                .build();
    }
}
