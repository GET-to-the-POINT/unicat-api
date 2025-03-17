package gettothepoint.unicatapi.presentation.controller.projetct;

import gettothepoint.unicatapi.application.service.OpenAiService;
import gettothepoint.unicatapi.application.service.ProjectService;
import gettothepoint.unicatapi.application.service.SectionService;
import gettothepoint.unicatapi.domain.dto.project.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @PostMapping(value="{projectId}/sections/{sectionId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImageResponse uploadImage(@PathVariable Long projectId, @PathVariable Long sectionId, @ModelAttribute UploadImageRequest uploadImageRequest) {
        return sectionService.uploadImage(projectId, sectionId, uploadImageRequest);
    }

    @PostMapping(value="{projectId}/sections/{sectionId}/image", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ImageResponse createImage(@PathVariable Long projectId, @PathVariable Long sectionId, @RequestBody CreateImageRequest createImageRequest) {
        return openAiService.createImage(projectId, sectionId, createImageRequest);
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

    @PostMapping("/{sectionId}/order")
    public Long updateSectionOrder(@PathVariable Long sectionId, @RequestBody int newOrder) {
        return sectionService.updateSectionSortOrder(sectionId, newOrder);
    }

    @PostMapping("/{projectId}/sections/{sectionId}/script")
    @ResponseStatus(HttpStatus.OK)
    public ScriptResponse refineScript(@PathVariable Long projectId, @PathVariable Long sectionId, @RequestBody @Valid ScriptRequest scriptRequest) {
        return openAiService.createScript(projectId, sectionId, scriptRequest);
    }
}