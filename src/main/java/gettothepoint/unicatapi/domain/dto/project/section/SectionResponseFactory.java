package gettothepoint.unicatapi.domain.dto.project.section;

import gettothepoint.unicatapi.domain.entity.project.Section;
import gettothepoint.unicatapi.filestorage.FileService;
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
                .frameUrl(section.getFrameKey() != null ? fileService.downloadFile(section.getFrameKey()).toString() : null)
                .alt(section.getAlt())
                .voiceModel(section.getVoiceModel())
                .transitionUrl(section.getTransitionKey() != null ? fileService.downloadFile(section.getTransitionKey()).toString() : null)
                .build();
    }
}
