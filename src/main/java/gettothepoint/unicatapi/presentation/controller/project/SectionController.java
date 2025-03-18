package gettothepoint.unicatapi.presentation.controller.project;

import gettothepoint.unicatapi.application.service.OpenAiService;
import gettothepoint.unicatapi.application.service.project.ProjectService;
import gettothepoint.unicatapi.application.service.project.SectionService;
import gettothepoint.unicatapi.domain.dto.project.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Project Section", description = "섹션 API")
@RestController
@RequestMapping("/projects/{projectId}/sections")
@RequiredArgsConstructor
public class SectionController {

    private final ProjectService projectService;
    private final SectionService sectionService;
    private final OpenAiService openAiService;

    @GetMapping
    public List<SectionResponse> getAllSections(@PathVariable Long projectId) {
        return projectService.getAllSections(projectId);
    }

    @PostMapping
    public Long createSection(@PathVariable Long projectId) {
        return sectionService.createSection(projectId);
    }

    @PostMapping(value = "/{sectionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImageResponse uploadImage(@PathVariable Long projectId, @PathVariable Long sectionId, @Valid @ModelAttribute UploadResourceRequest uploadResourceRequest) {
        return sectionService.uploadImage(projectId, sectionId, uploadResourceRequest);
    }

    @PostMapping("/{sectionId}/order")
    public Long updateSectionOrder(@PathVariable Long sectionId, @RequestBody int newOrder) {
        return sectionService.updateSectionSortOrder(sectionId, newOrder);
    }

    @PreAuthorize("@projectService.verifyProjectOwner(#projectId, jwt.getSubject())")
    @PostMapping(value = "/{sectionId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CreateResourceResponse createImageAndScript(@AuthenticationPrincipal Jwt jwt, @PathVariable Long projectId, @PathVariable Long sectionId, @RequestParam(required = false) String type, @Valid @RequestBody PromptRequest scriptRequest) {
        return openAiService.createContent(projectId, sectionId, type, scriptRequest);
    }

}
