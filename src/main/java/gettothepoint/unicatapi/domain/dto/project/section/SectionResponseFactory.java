package gettothepoint.unicatapi.domain.dto.project.section;

import gettothepoint.unicatapi.domain.entity.project.Section;
import gettothepoint.unicatapi.filestorage.application.port.in.FileDownloadUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SectionResponseFactory {

    private final FileDownloadUseCase fileDownloadUseCase;

    public SectionResponse fromEntity(Section section) {
        return SectionResponse.builder()
                .id(section.getId())
                .script(section.getScript())
                .sortOrder(section.getSortOrder())
                .contentUrl(section.getContentKey() != null ? fileDownloadUseCase.downloadFile(section.getContentKey()).toString() : null)
                .audioUrl(section.getAudioKey() != null ? fileDownloadUseCase.downloadFile(section.getAudioKey()).toString() : null)
                .frameUrl(section.getFrameKey() != null ? fileDownloadUseCase.downloadFile(section.getFrameKey()).toString() : null)
                .alt(section.getAlt())
                .voiceModel(section.getVoiceModel())
                .transitionUrl(section.getTransitionKey() != null ? fileDownloadUseCase.downloadFile(section.getTransitionKey()).toString() : null)
                .build();
    }
}
