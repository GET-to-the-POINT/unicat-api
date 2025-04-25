//package gettothepoint.unicatapi.presentation.controller.project;
//
//import com.google.api.services.youtubeAnalytics.v2.model.QueryResponse;
//import gettothepoint.unicatapi.application.service.media.ArtifactService;
//import gettothepoint.unicatapi.application.service.project.ProjectService;
//import gettothepoint.unicatapi.application.service.youtube.YouTubeAnalyticsProxyService;
//import gettothepoint.unicatapi.domain.dto.project.project.ProjectRequest;
//import gettothepoint.unicatapi.domain.dto.project.project.ProjectRequestWithoutFile;
//import gettothepoint.unicatapi.domain.dto.project.project.ProjectResponse;
//import gettothepoint.unicatapi.domain.dto.project.PromptRequest;
//import gettothepoint.unicatapi.infrastructure.progress.ProgressManager;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//import org.springdoc.core.annotations.ParameterObject;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.web.PageableDefault;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
//import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
//import org.springframework.security.oauth2.core.OAuth2AccessToken;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
//
//import java.util.Map;
//import java.util.concurrent.Executors;
//
//@Log4j2
//@Tag(name = "Project", description = "프로젝트 API")
//@RestController
//@RequestMapping("/projects")
//@RequiredArgsConstructor
//public class ProjectController {
//
//    private final ProjectService projectService;
//    private final YouTubeAnalyticsProxyService youtubeAnalyticsProxyService;
//    private final ArtifactService artifactService;
//    private final ProgressManager progressManager;
//
//    @Operation(
//            summary = "프로젝트 목록 조회",
//            description = "페이지, 정렬 옵션을 활용하여 모든 프로젝트를 리스트 형태로 조회합니다. (예: 최신순 정렬)"
//    )
//    @GetMapping
//    public Page<ProjectResponse> getAll(
//            @ParameterObject
//            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
//            Pageable pageable
//    ) {
//        return projectService.getAll(pageable);
//    }
//
//    @Operation(
//            summary = "프로젝트 생성",
//            description = "multipart/form-data를 사용해 새 프로젝트를 생성합니다. 파일을 포함한 다양한 정보를 함께 전달하세요."
//    )
//    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @ResponseStatus(HttpStatus.CREATED)
//    public ProjectResponse create(@AuthenticationPrincipal Jwt jwt,
//                                  @ModelAttribute ProjectRequest request) {
//        Long memberId = Long.valueOf(jwt.getSubject());
//        return projectService.create(memberId, request);
//    }
//
//    @Operation(
//            summary = "프로젝트 생성",
//            description = "JSON 형식의 요청 바디로 새로운 프로젝트를 생성합니다. 파일 없이 텍스트 정보만 제공됩니다."
//    )
//    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseStatus(HttpStatus.CREATED)
//    public ProjectResponse create(@AuthenticationPrincipal Jwt jwt,
//                                  @RequestBody ProjectRequestWithoutFile request) {
//        Long memberId = Long.valueOf(jwt.getSubject());
//        return projectService.create(memberId, request);
//    }
//
//    @Operation(
//            summary = "프로젝트 상세 조회",
//            description = "프로젝트 ID를 통해 해당 프로젝트의 상세 정보를 반환합니다. 모든 세부 정보가 포함되어 있습니다."
//    )
//    @PreAuthorize("@projectService.verifyProjectOwner(#jwt.subject, #projectId)")
//    @GetMapping("/{projectId}")
//    public ProjectResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long projectId) {
//        return projectService.get(projectId);
//    }
//
//    @Operation(
//            summary = "아티팩트 생성",
//            description = "프로젝트 아티팩트를 생성합니다. 요청 파라미터 'type'에 따라 유튜브 또는 비디오 업로드 기능을 제공합니다."
//    )
//    @PreAuthorize("@projectService.verifyProjectOwner(#jwt.subject, #projectId)")
//    @PostMapping("/{projectId}")
//    @ResponseStatus(HttpStatus.ACCEPTED)
//    public void createArtifact(@AuthenticationPrincipal Jwt jwt,
//                               @PathVariable Long projectId,
//                               @RequestParam(defaultValue = "artifact") String type,
//                               @Parameter(hidden = true) @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {
//        OAuth2AccessToken token = authorizedClient.getAccessToken();
//        if (token != null && "youtube".equals(type) || "vimeo".equals(type)) {
//            artifactService.build(projectId, type, token);
//        } else {
//            artifactService.build(projectId);
//        }
//    }
//
//    @Operation(
//            summary = "프로젝트 정보 수정",
//            description = "프로젝트 정보를 업데이트합니다. multipart/form-data 형식을 ���용하여 파일 및 텍스트 정보를 함께 전달할 수 있습니다."
//    )
//    @PreAuthorize("@projectService.verifyProjectOwner(#jwt.subject, #projectId)")
//    @PatchMapping(value = "/{projectId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void update(@AuthenticationPrincipal Jwt jwt,
//                       @PathVariable Long projectId,
//                       @ModelAttribute ProjectRequest request) {
//        projectService.update(projectId, request);
//    }
//
//    @Operation(
//            summary = "프로젝트 정보 수정",
//            description = "JSON 형식의 요청 바디로 프로젝트 정보를 수정합니다. 파일 없이 텍스트만 업데이트됩니다."
//    )
//    @PreAuthorize("@projectService.verifyProjectOwner(#jwt.subject, #projectId)")
//    @PatchMapping(value = "/{projectId}", consumes = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void update(@AuthenticationPrincipal Jwt jwt,
//                       @PathVariable Long projectId,
//                       @RequestBody ProjectRequestWithoutFile request) {
//        projectService.update(projectId, request);
//    }
//
//    @Operation(
//            summary = "유튜브 분석 데이터 조회",
//            description = "현재 인증된 사용자의 유튜브 분석 데이터를 쿼리 파라미터와 함께 조회합니다. (예: startDate, endDate, metrics)"
//    )
//    @GetMapping("/youtube-analytics")
//    @PreAuthorize("isAuthenticated()")
//    public QueryResponse getYouTubeAnalytics(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient, @RequestParam Map<String, String> queryParams) {
//        Long memberId = Long.valueOf(authorizedClient.getPrincipalName());
//        return youtubeAnalyticsProxyService.getYouTubeAnalyticsData(authorizedClient.getAccessToken(), queryParams, memberId);
//    }
//
//    @Operation(
//            summary = "원스탭 아티팩트 생성",
//            description = "프롬프트 정보를 기반으로 OpenAI를 사용해 자동으로 콘텐츠를 생성하고 프로젝트 아티팩트를 만듭니다. 간편하게 전체 콘텐츠를 구성할 수 있습니다."
//    )
//    @PostMapping("/one-step")
//    @ResponseStatus(HttpStatus.ACCEPTED)
//    public void autoBuild(@AuthenticationPrincipal Jwt jwt, PromptRequest promptRequest) {
//        Long memberId = Long.valueOf(jwt.getSubject());
//        Long projectId = artifactService.oneStepAutoArtifact(memberId, promptRequest);
//        artifactService.build(projectId,"artifact", null);
//    }
//
//    @Operation(
//            summary = "진행률 SSE 테스트",
//            description = "샘플 데이터를 사용해 0~100%까지 진행률을 SSE 방식으로 전달합니다. 프론트엔드에서 진행률 UI를 테스트할 수 있습니다."
//    )
//    @PreAuthorize("@projectService.verifyProjectOwner(#jwt.subject, #projectId)")
//    @GetMapping("/{projectId}/progress")
//    public SseEmitter progress(@AuthenticationPrincipal Jwt jwt, @PathVariable String projectId) {
//        SseEmitter emitter = new SseEmitter(3 * 60 * 1000L);
//        Executors.newSingleThreadExecutor().submit(() -> {
//            try {
//                for (int i = 0; i <= 100; i += 10) {
//                    emitter.send(SseEmitter.event()
//                            .name("progress")
//                            .data(i));
//                    Thread.sleep(500);
//                }
//                emitter.complete();
//            } catch (Exception e) {
//                emitter.completeWithError(e);
//            }
//        });
//        return emitter;
//    }
//
////    @Operation(
////            summary = "프로젝트 업로드 진행률 구독",
////            description = "특정 프로젝트의 유튜브 업로드 진행률을 SSE 방식으로 구독합니다. projectId 경로 변수를 사용합니다."
////    )
////    @GetMapping("/{projectId}/progress")
////    public SseEmitter subscribe(@PathVariable Long projectId) {
////        return progressManager.createEmitter(projectId);
////    }
//}
