package gettothepoint.unicatapi.presentation.controller.projetct;

import gettothepoint.unicatapi.application.service.ProjectService;
import gettothepoint.unicatapi.application.service.SectionService;
import gettothepoint.unicatapi.domain.dto.project.ProjectResponse;
import gettothepoint.unicatapi.domain.dto.project.SectionRequest;
import gettothepoint.unicatapi.domain.dto.project.SectionResponse;
import gettothepoint.unicatapi.domain.dto.storage.StorageUpload;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @PostMapping(value="/{sectionId}/image", consumes = "multipart/form-data")
    public StorageUpload uploadImage(@PathVariable Long sectionId, @RequestParam("file") MultipartFile file) {
        return sectionService.uploadImage(sectionId, file);
    }

    @PostMapping("/{sectionId}/script")
    public void uploadScript(@PathVariable Long sectionId, @RequestBody String script) {
        sectionService.uploadScript(sectionId, script);
    }

    @GetMapping("/{projectId}/sections")
    public List<SectionResponse> getAllSections(@PathVariable Long projectId) {
        return projectService.getAllSections(projectId);
    }

    @PostMapping("/{projectId}")
    public void createVideo(@PathVariable Long projectId, @RequestBody List<SectionRequest> sectionRequests) {
        projectService.createVideo(sectionRequests);
    }

    @PostMapping("/{sectionId}/updateOrder")
    public Long updateSectionOrder(@PathVariable Long sectionId, @RequestBody int newOrder) {
        return sectionService.updateSectionSortOrder(sectionId, newOrder);
    }

}