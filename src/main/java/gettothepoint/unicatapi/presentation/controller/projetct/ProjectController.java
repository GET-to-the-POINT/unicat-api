package gettothepoint.unicatapi.presentation.controller.projetct;

import gettothepoint.unicatapi.application.service.OpenAiService;
import gettothepoint.unicatapi.application.service.ProjectService;
import gettothepoint.unicatapi.application.service.SectionService;
import gettothepoint.unicatapi.domain.dto.project.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final SectionService sectionService;
    private final OpenAiService openAiService;

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

    @PostMapping("/{projectId}/sections")
    public Long createSection(@PathVariable Long projectId) {
       return sectionService.createSection(projectId);
    }

    @PostMapping(value="/{projectId}/sections/{sectionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImageResponse uploadImage(@PathVariable Long projectId, @PathVariable Long sectionId, @Valid @ModelAttribute UploadResourceRequest uploadResourceRequest) {
        return sectionService.uploadImage(projectId, sectionId, uploadResourceRequest);
    }

    @PostMapping("/{projectId}/sections/{sectionId}/script")
    public void uploadScript(@PathVariable Long projectId, @PathVariable Long sectionId, @RequestBody String script) {
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

    @PostMapping("/{sectionId}/order")
    public Long updateSectionOrder(@PathVariable Long sectionId, @RequestBody int newOrder) {
        return sectionService.updateSectionSortOrder(sectionId, newOrder);
    }

    @PreAuthorize("@projectService.verifyProjectOwner(#projectId, jwt.getSubject())")
    @PostMapping(value="/{projectId}/sections/{sectionId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CreateResourceResponse createImageAndScript(@AuthenticationPrincipal Jwt jwt, @PathVariable Long projectId, @PathVariable Long sectionId,
                                                       @RequestParam(required = false) String type, @Valid @RequestBody PromptRequest scriptRequest) {
        return openAiService.createContent(projectId, sectionId, type, scriptRequest);
    }

    @GetMapping("/{projectId}")
    public ProjectDto getProject(@PathVariable Long projectId) {
        return projectService.getProject(projectId);
    }
}