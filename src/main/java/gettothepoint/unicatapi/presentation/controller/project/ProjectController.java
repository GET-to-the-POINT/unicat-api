package gettothepoint.unicatapi.presentation.controller.project;

import com.google.api.services.youtubeAnalytics.v2.model.QueryResponse;
import gettothepoint.unicatapi.application.service.media.ArtifactService;
import gettothepoint.unicatapi.application.service.project.ProjectService;
import gettothepoint.unicatapi.application.service.youtube.YouTubeAnalyticsProxyService;
import gettothepoint.unicatapi.domain.dto.project.ProjectResponse;
import gettothepoint.unicatapi.domain.dto.project.PromptRequest;
import gettothepoint.unicatapi.infrastructure.progress.ProgressManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.Executors;

@Log4j2
@Tag(name = "Project", description = "프로젝트 API")
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final YouTubeAnalyticsProxyService youtubeAnalyticsProxyService;
    private final ArtifactService artifactService;
    private final ProgressManager progressManager;

    @Operation(
            summary = "프로젝트 목록 조회",
            description = "페이지 번호, 사이즈, 정렬 옵션을 사용하여 모든 프로젝트를 페이징 단위로 조회하는 API입니다."
    )
    @GetMapping
    public Page<ProjectResponse> getAll(
            @ParameterObject
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return projectService.getAll(pageable);
    }

    @Operation(
            summary = "프로젝트 생성",
            description = "현재 로그인 사용자의 정보를 기반으로 새로운 프로젝트를 생성합니다. JWT 토큰의 subject 값을 memberId로 사용합니다."
    )
    @PostMapping()
    public ProjectResponse create(@AuthenticationPrincipal Jwt jwt) {
        Long memberId = Long.valueOf(jwt.getSubject());
        return projectService.create(memberId);
    }

    @Operation(
            summary = "프로젝트 상세 조회",
            description = "경로 변수 projectId를 사용해 특정 프로젝트의 상세 정보를 반환합니다."
    )
    @GetMapping("/{projectId}")
    public ProjectResponse get(@PathVariable Long projectId) {
        return projectService.get(projectId);
    }

    @Operation(
            summary = "아티팩트 생성",
            description = "프로젝트에 대해 아티팩트를 생성합니다. type이 'youtube' 또는 'vimeo'인 경우 인증된 액세스 토큰을 사용하며, 기본 타입은 'artifact'입니다."
    )
    @PostMapping("/{projectId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void createArtifact(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "artifact") String type,
            @Parameter(hidden = true) @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient
    ) {
        OAuth2AccessToken token = null;
        if ("youtube".equals(type) || "vimeo".equals(type)) {
            token = authorizedClient.getAccessToken();
        }
        artifactService.buildAsync(projectId, type, token);
    }

    @Operation(
            summary = "유튜브 분석 조회",
            description = "현재 인증된 사용자의 유튜브 분석 데이터를 추가 쿼리 파라미터와 함께 조회합니다."
    )
    @GetMapping("/youtube-analytics")
    @PreAuthorize("isAuthenticated()")
    public QueryResponse getYouTubeAnalytics(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient, @RequestParam Map<String, String> queryParams) {
        Long memberId = Long.valueOf(authorizedClient.getPrincipalName());
        return youtubeAnalyticsProxyService.getYouTubeAnalyticsData(authorizedClient.getAccessToken(), queryParams, memberId);
    }

    @Operation(
            summary = "원스탭 아티팩트 생성",
            description = "프롬프트 정보를 기반으로 OpenAI를 사용해 콘텐츠를 자동 생성하고, 이를 기반으로 프로젝트 아티팩트를 생성하는 API입니다."
    )
    @PostMapping("/one-step")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void autoBuild(@AuthenticationPrincipal Jwt jwt, PromptRequest promptRequest) {
        Long memberId = Long.valueOf(jwt.getSubject());
        Long projectId = artifactService.oneStepAutoArtifact(memberId, promptRequest);
        artifactService.buildAsync(projectId,"artifact", null).join();
    }

    @Operation(
            summary = "샘플 진행률 SSE 테스트",
            description = "샘플 데이터를 사용하여 SSE 방식으로 진행률 이벤트(0~100%)를 테스트하는 API입니다."
    )
    @GetMapping("/{projectId}/progress")
    public SseEmitter progress(@PathVariable String projectId) {
        SseEmitter emitter = new SseEmitter(3 * 60 * 1000L);
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                for (int i = 0; i <= 100; i += 10) {
                    emitter.send(SseEmitter.event()
                            .name("progress")
                            .data(i));
                    Thread.sleep(500);
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

//    @Operation(
//            summary = "프로젝트 업로드 진행률 구독",
//            description = "특정 프로젝트의 유튜브 업로드 진행률을 SSE 방식으로 구독합니다. projectId 경로 변수를 사용합니다."
//    )
//    @GetMapping("/{projectId}/progress")
//    public SseEmitter subscribe(@PathVariable Long projectId) {
//        return progressManager.createEmitter(projectId);
//    }
}
