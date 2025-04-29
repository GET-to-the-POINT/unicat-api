package gettothepoint.unicatapi.application.service.project;

import gettothepoint.unicatapi.domain.dto.project.ResourceResponse;
import gettothepoint.unicatapi.domain.dto.project.section.SectionResourceRequest;
import gettothepoint.unicatapi.domain.dto.project.section.SectionResourceRequestWithoutFile;
import gettothepoint.unicatapi.domain.dto.project.section.SectionResponse;
import gettothepoint.unicatapi.domain.dto.project.section.SectionResponseFactory;
import gettothepoint.unicatapi.domain.entity.project.Project;
import gettothepoint.unicatapi.domain.entity.project.Section;
import gettothepoint.unicatapi.domain.repository.SectionRepository;
import gettothepoint.unicatapi.filestorage.FileService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SectionService {

    @Value("${app.supertone.default-voice-id}")
    private String supertoneDefaultVoiceId;

    private final ProjectService projectService;
    private final SectionRepository sectionRepository;
    private final FileService fileService;
    private final SectionResponseFactory sectionResponseFactory;
    private static final String SECTION_NOT_FOUND_MSG = "Section not found with id: ";

    // 컬렉션 페이지네이션
    public Page<SectionResponse> getAll(Long projectId, Pageable pageable) {
        Page<Section> sections = sectionRepository.findAllByProjectIdOrderBySortOrderAsc(projectId, pageable);
        return sections.map(sectionResponseFactory::fromEntity);
    }

    // 컬렉션 전체
    public List<SectionResponse> getSectionResponseAll(Long projectId) {
        List<Section> sections = sectionRepository.findAllByProjectIdOrderBySortOrderAsc(projectId);
        return sections.stream().map(sectionResponseFactory::fromEntity).toList();
    }

    public List<Section> getSectionAll(Long projectId) {
        return sectionRepository.findAllByProjectIdOrderBySortOrderAsc(projectId);
    }

    // 싱글
    public SectionResponse get(Long projectId, Long sectionId) {
        Section section = this.getOrElseThrow(projectId, sectionId);
        return sectionResponseFactory.fromEntity(section);
    }

    public SectionResponse create(Long projectId) {
        Project project = projectService.getOrElseThrow(projectId);
        Section newSection = Section.builder()
                .project(project)
                .build();
        return create(newSection);
    }

    public SectionResponse create(Long projectId, SectionResourceRequestWithoutFile sectionResourceRequestWithoutFile) {
        Project project = projectService.getOrElseThrow(projectId);
        Section newSection = Section.builder()
                .project(project)
                .voiceModel(sectionResourceRequestWithoutFile.voiceModel())
                .alt(sectionResourceRequestWithoutFile.alt())
                .script(sectionResourceRequestWithoutFile.script())
                .transitionKey(sectionResourceRequestWithoutFile.transitionKey())
                .build();
        return create(newSection);
    }

    // 생성
    @Transactional
    public SectionResponse create(Long projectId, SectionResourceRequest sectionResourceRequest) {
        Project project = projectService.getOrElseThrow(projectId);

        String voiceModel = StringUtils.hasText(sectionResourceRequest.voiceModel()) ? sectionResourceRequest.voiceModel() : supertoneDefaultVoiceId;
        String contentKey = null;
        if (sectionResourceRequest.multipartFile() != null) {
            contentKey = fileService.uploadFile(sectionResourceRequest.multipartFile());
        }

        Section newSection = Section.builder()
                        .project(project)
                        .voiceModel(voiceModel)
                        .alt(sectionResourceRequest.alt())
                        .script(sectionResourceRequest.script())
                        .contentKey(contentKey)
                        .transitionKey(sectionResourceRequest.transitionKey())
                        .build();
        return create(newSection);
    }

    public ResourceResponse update(Long productId, Long sectionId, SectionResourceRequestWithoutFile sectionResourceRequestWithoutFile) {
        SectionResourceRequest request = SectionResourceRequest.fromSectionResourceRequestWithoutFile(sectionResourceRequestWithoutFile);
        return update(productId, sectionId, request);
    }

    public ResourceResponse update(Long projectId, Long sectionId, SectionResourceRequest sectionResourceRequest) {
        Section section = this.getOrElseThrow(projectId, sectionId);
        boolean changed = false;

        if (sectionResourceRequest.script() != null) {
            section.setScript(sectionResourceRequest.script());
            changed = true;
        }
        if (sectionResourceRequest.alt() != null) {
            section.setAlt(sectionResourceRequest.alt());
            changed = true;
        }
        if (sectionResourceRequest.multipartFile() != null && !sectionResourceRequest.multipartFile().isEmpty()) {
            String uploadedKey = fileService.uploadFile(sectionResourceRequest.multipartFile());
            section.setContentKey(uploadedKey);
            changed = true;
        }
        if (sectionResourceRequest.transitionKey() != null) {
            section.setTransitionKey(sectionResourceRequest.transitionKey());
            changed = true;
        }

        if (changed) {
            // 섹션 데이터가 변경되면 새로 빌드되어야하기 때문에 널이 되어야 한다.
            section.setFrameKey(null);
            section.getProject().setArtifactKey(null);
            projectService.update(section.getProject());
        }

        this.update(section);

        Project project = section.getProject();
        project.setArtifactKey(null);
        projectService.update(project);

        return ResourceResponse.fromEntity(section);
    }

    public SectionResponse create(Section section) {
        update(section);
        return sectionResponseFactory.fromEntity(section);
    }

    public void updateSectionSortOrder(Long sectionId, int newOrder) {
        Section section = sectionRepository.findById(sectionId).orElseThrow(() -> new EntityNotFoundException(SECTION_NOT_FOUND_MSG + sectionId));
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

    }

    public Section getOrElseThrow(Long projectId, Long sectionId) {
        return sectionRepository.findByProjectIdAndId(projectId, sectionId)
                .orElseThrow(() -> new EntityNotFoundException(SECTION_NOT_FOUND_MSG + sectionId));
    }

    public Section getOrElseThrow(Long sectionId) {
        return sectionRepository.findById(sectionId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "섹션을 찾을 수 없습니다."));
    }

    public void update(Section section) {
        sectionRepository.save(section);
        sectionRepository.flush();
    }

    public void delete(Long projectId, Long sectionId) {
        Section section = this.getOrElseThrow(projectId, sectionId);
        Project project = projectService.getOrElseThrow(projectId);
        sectionRepository.delete(section);
        reSortOrder(project.getSections());
    }

    private void reSortOrder(List<Section> sections) {
    sections.sort(Comparator.comparing(Section::getSortOrder));
        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            section.setSortOrder(i + 1L);
        }
    }
}
