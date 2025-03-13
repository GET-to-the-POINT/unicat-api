package gettothepoint.unicatapi.application.service;

import gettothepoint.unicatapi.application.service.storage.FileStorageService;
import gettothepoint.unicatapi.domain.dto.storage.StorageUpload;
import gettothepoint.unicatapi.domain.entity.dashboard.Project;
import gettothepoint.unicatapi.domain.entity.dashboard.Section;
import gettothepoint.unicatapi.domain.repository.ProjectRepository;
import gettothepoint.unicatapi.domain.repository.SectionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SectionService {

    private final SectionRepository sectionRepository;
    private final ProjectRepository projectRepository;
    private final FileStorageService fileStorageService;

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

    public StorageUpload uploadImage(Long sectionId, MultipartFile file) {

        StorageUpload storageUpload = fileStorageService.uploadFile(file);

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section not found with id: " + sectionId));

        section.setUploadImageUrl(storageUpload.url());
        sectionRepository.save(section);

        return storageUpload;
    }

    public void uploadScript(Long sectionId, String script) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section not found with id: " + sectionId));

        section.setScript(script);
        sectionRepository.save(section);
    }
}
