package gettothepoint.unicatapi.application.service.project;

import gettothepoint.unicatapi.application.service.storage.StorageService;
import gettothepoint.unicatapi.domain.dto.storage.UploadResult;
import gettothepoint.unicatapi.domain.dto.project.ResourceResponse;
import gettothepoint.unicatapi.domain.dto.project.SectionResourceRequest;
import gettothepoint.unicatapi.domain.dto.project.SectionResponse;
import gettothepoint.unicatapi.domain.entity.dashboard.Project;
import gettothepoint.unicatapi.domain.entity.dashboard.Section;
import gettothepoint.unicatapi.domain.repository.SectionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SectionService {

    private final ProjectService projectService;
    private final SectionRepository sectionRepository;
    private final StorageService storageService;
    private static final String SECTION_NOT_FOUND_MSG = "Section not found with id: ";

    // 컬렉션 페이지네이션
    public Page<SectionResponse> getAll(Long projectId, Pageable pageable) {
        Page<Section> sections = sectionRepository.findAllByProjectIdOrderBySortOrderAsc(projectId, pageable);
        return sections.map(SectionResponse::fromEntity);
    }

    // 컬렉션 전체
    public List<SectionResponse> getAll(Long projectId) {
        List<Section> sections = sectionRepository.findAllByProjectIdOrderBySortOrderAsc(projectId);
        return sections.stream().map(SectionResponse::fromEntity).toList();
    }

    // 싱글
    public SectionResponse get(Long projectId, Long sectionId) {
        Section section = this.getOrElseThrow(projectId, sectionId);
        return SectionResponse.fromEntity(section);
    }

    // 생성
    public SectionResponse create(Long projectId) {
        Project project = projectService.getOrElseThrow(projectId);
        Long sortOrder = sectionRepository.findMaxSortOrderByProject(projectId);
        Section newSection = Section.builder()
                        .project(project)
                        .sortOrder(sortOrder + 1)
                        .build();
        sectionRepository.save(newSection);
        return SectionResponse.fromEntity(newSection);
    }

    public ResourceResponse uploadResource(Long projectId, Long sectionId, SectionResourceRequest sectionResourceRequest) {
        Section section = this.getOrElseThrow(projectId, sectionId);

        if (!sectionResourceRequest.script().isEmpty()) section.setScript(sectionResourceRequest.script());
        if (!sectionResourceRequest.alt().isEmpty()) section.setAlt(sectionResourceRequest.alt());
        if (!sectionResourceRequest.multipartFile().isEmpty()) {
            String uploadResult = storageService.upload(sectionResourceRequest.multipartFile());
            section.setResourceUrl(uploadResult);
        }
        this.update(section);
        return ResourceResponse.fromEntity(section);
    }


    public Long updateSectionSortOrder(Long sectionId, int newOrder) {
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

        return section.getSortOrder();
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
}
