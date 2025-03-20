package gettothepoint.unicatapi.application.service.project;

import gettothepoint.unicatapi.application.service.TextToSpeechService;
import gettothepoint.unicatapi.application.service.storage.AbstractStorageService;
import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.common.util.MultipartFileUtil;
import gettothepoint.unicatapi.domain.dto.project.ImageResponse;
import gettothepoint.unicatapi.domain.dto.project.SectionRequest;
import gettothepoint.unicatapi.domain.dto.project.UploadResourceRequest;
import gettothepoint.unicatapi.domain.entity.dashboard.Project;
import gettothepoint.unicatapi.domain.entity.dashboard.Section;
import gettothepoint.unicatapi.domain.repository.ProjectRepository;
import gettothepoint.unicatapi.domain.repository.SectionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SectionService {

    private final SectionRepository sectionRepository;
    private final ProjectRepository projectRepository;
    private final AbstractStorageService abstractStorageService;
    private final TextToSpeechService textToSpeechService;
    private final MessageSource messageSource;
    private final AppProperties appProperties;
    private static final String SECTION_NOT_FOUND_MSG = "Section not found with id: ";

    public Long createSection(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));

        List<Section> sections = sectionRepository.findAllByProject(project);

        Long sortOrder = (long) (sections.size() + 1);

        Section section = Section.builder()
                .project(project)
                .sortOrder(sortOrder)
                .build();
        sectionRepository.save(section);

        return section.getId();
    }

    public ImageResponse uploadImage(Long projectId, Long sectionId, UploadResourceRequest uploadResourceRequest) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException(SECTION_NOT_FOUND_MSG + sectionId));

        MultipartFile file = uploadResourceRequest.image();

        String url = abstractStorageService.upload(file);

        section.setAlt(uploadResourceRequest.alt());
        section.setImageUrl(url);
        sectionRepository.save(section);

        return new ImageResponse(url, uploadResourceRequest.alt());
    }

    public void uploadScript(Long sectionId, String script) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException(SECTION_NOT_FOUND_MSG + sectionId));

        section.setScript(script);
        sectionRepository.save(section);
    }

    public void createTextToSpeech(List<SectionRequest> sectionRequests) {

        sectionRequests.forEach(request -> {
            Section section = sectionRepository.findById(request.sectionId())
                    .orElseThrow(() -> new EntityNotFoundException(SECTION_NOT_FOUND_MSG + request.sectionId()));
            String script = section.getScript();
            String voiceName = request.voiceName();
            String filePath = appProperties.tts().filePath() + section.getId() + appProperties.tts().fileExtension();

            try {
                textToSpeechService.createAndSaveTTSFile(script, voiceName, filePath);
                File file = new File(filePath);
                section.setTtsUrl(uploadTTSFile(file));
                sectionRepository.save(section);
            } catch (IOException e) {
                log.error("Error saving TTS file for section {}: {}", section.getId(), e.getMessage(), e);
                String errorMessage = messageSource.getMessage("error.unknown", null, LocaleContextHolder.getLocale());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage, e);
            }
        });
    }

    public String uploadTTSFile(File file) {
        MultipartFileUtil multipartFile = new MultipartFileUtil(file, file.getName(), "audio/mpeg");
        return abstractStorageService.upload(multipartFile);
    }

    @Transactional
    public Long updateSectionSortOrder(Long sectionId, int newOrder) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException(SECTION_NOT_FOUND_MSG + sectionId));
        Project project = section.getProject();

        List<Section> sections = sectionRepository.findAllByProjectIdOrderBySortOrderAsc(project.getId());

        int currentIndex = section.getSortOrder().intValue() - 1;

        sections.remove(currentIndex);

        if (newOrder < 1) {
            newOrder = 1;
        } else if (newOrder > sections.size() + 1) {
            newOrder = sections.size() + 1;
        }

        sections.add(newOrder - 1, section);

        for (int i = 0; i < sections.size(); i++) {
            Section s = sections.get(i);
            s.setSortOrder((long) (i + 1));
            sectionRepository.save(s);
        }

        return section.getSortOrder();
    }

    public List<Section> getAllSortedBySortOrderOrElseThrow(Long projectId) {
        // TODO: 구현 필요
        return null;
    }
}
