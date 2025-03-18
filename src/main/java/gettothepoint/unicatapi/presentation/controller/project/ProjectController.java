package gettothepoint.unicatapi.presentation.controller.project;

import com.google.api.services.youtubeAnalytics.v2.model.QueryResponse;
import gettothepoint.unicatapi.application.service.project.ProjectService;
import gettothepoint.unicatapi.application.service.OpenAiService;
import gettothepoint.unicatapi.application.service.ProjectService;
import gettothepoint.unicatapi.application.service.SectionService;
import gettothepoint.unicatapi.application.service.ffmpeg.ArtifactService;
import gettothepoint.unicatapi.application.service.youtube.YouTubeAnalyticsProxyService;
import gettothepoint.unicatapi.domain.dto.project.ProjectDto;
import gettothepoint.unicatapi.domain.dto.project.ProjectResponse;
import gettothepoint.unicatapi.domain.dto.project.SectionRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import gettothepoint.unicatapi.domain.dto.project.*;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Tag(name = "Project", description = "프로젝트 API")
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final YouTubeAnalyticsProxyService youtubeAnalyticsProxyService;
    private final ArtifactService artifactService;

    @GetMapping()
    public ProjectResponse getProjects(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "createdAt,desc") String sort) {
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

//    @PostMapping("/{projectId}")
//    public void createVideo(@PathVariable Long projectId, @RequestBody List<SectionRequest> sectionRequests) {
//        projectService.createVideo(sectionRequests);
//    }

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


    @PostMapping("/{projectId}")
    public ResponseEntity<String> createArtifact(@PathVariable("projectId") Long projectId,
                                                 @RequestParam(name = "type", required = false, defaultValue = "artifact") String type,
                                                 @Parameter(hidden = true)
                                                 @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {
        try {
            OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
            String result = artifactService.handleArtifactRequest(projectId, type, accessToken);
            return ResponseEntity.accepted().body(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("아티팩트 생성 실패: " + e.getMessage());
        }
    }

    @GetMapping("/{projectId}")
    public ProjectDto getProject(@PathVariable Long projectId) {
        return projectService.getProject(projectId);
    }

    @PostMapping("/{projectId}")
    public void createVideo(@PathVariable Long projectId, @RequestBody List<SectionRequest> sectionRequests) {
        projectService.createVideo(sectionRequests);
    }

    @GetMapping("/youtube-analytics")
    @PreAuthorize("isAuthenticated()")
    public QueryResponse getYouTubeAnalytics(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient, @RequestParam Map<String, String> queryParams) {
        Long memberId = Long.valueOf(authorizedClient.getPrincipalName());
        return youtubeAnalyticsProxyService.getYouTubeAnalyticsData(authorizedClient.getAccessToken(), queryParams, memberId);
    }

}