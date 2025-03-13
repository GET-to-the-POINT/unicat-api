package gettothepoint.unicatapi.presentation.controller.projetct;

import gettothepoint.unicatapi.application.service.ProjectService;
import gettothepoint.unicatapi.application.service.SectionService;
import gettothepoint.unicatapi.domain.dto.project.ProjectResponse;
import gettothepoint.unicatapi.domain.dto.storage.StorageUpload;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final SectionService sectionService;

    @GetMapping() // 프로젝트 조회 API
    public ProjectResponse getProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        return projectService.getProjects(page, size, sort);
    }

    @PostMapping()
    public Long createProject(@AuthenticationPrincipal Jwt jwt) {
        Long memberId = Long.valueOf(jwt.getSubject());
        return projectService.createProject(memberId);
    }

    @PostMapping("/{projectId}/section")
    public Long createSection(@PathVariable Long projectId) {
       return sectionService.createSection(projectId);
    }

    @PostMapping(value="/{sectionId}/upload-image", consumes = "multipart/form-data")
    public StorageUpload uploadImage(@PathVariable Long sectionId, @RequestParam("file") MultipartFile file) {
        return sectionService.uploadImage(sectionId, file);
    }
}