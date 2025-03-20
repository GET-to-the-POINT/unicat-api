package gettothepoint.unicatapi.presentation.controller.project;

import com.google.api.services.youtubeAnalytics.v2.model.QueryResponse;
import gettothepoint.unicatapi.application.service.media.ArtifactService;
import gettothepoint.unicatapi.application.service.project.ProjectService;
import gettothepoint.unicatapi.application.service.youtube.YouTubeAnalyticsProxyService;
import gettothepoint.unicatapi.domain.dto.project.ProjectDto;
import gettothepoint.unicatapi.domain.dto.project.ProjectResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{projectId}")
    public ProjectDto getProject(@PathVariable Long projectId) {
        return projectService.getProject(projectId);
    }

    @PostMapping("/{projectId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void createArtifact(@PathVariable("projectId") Long projectId, @RequestParam(name = "type", required = false, defaultValue = "artifact") String type, @Parameter(hidden = true) @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {
        switch (type) {
            case "youtube", "vimeo":
                OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
                artifactService.create(projectId, type, accessToken);
                return;
            default:
                artifactService.create(projectId);
        }
    }

    @GetMapping("/youtube-analytics")
    @PreAuthorize("isAuthenticated()")
    public QueryResponse getYouTubeAnalytics(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient, @RequestParam Map<String, String> queryParams) {
        Long memberId = Long.valueOf(authorizedClient.getPrincipalName());
        return youtubeAnalyticsProxyService.getYouTubeAnalyticsData(authorizedClient.getAccessToken(), queryParams, memberId);
    }

}