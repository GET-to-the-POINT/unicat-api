package gettothepoint.unicatapi.presentation.controller.video;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.*;
import gettothepoint.unicatapi.application.service.video.YouTubeUploadService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/youtube")
@Tag(name = "YouTube Upload", description = "YouTube 동영상 업로드 관련 API")
public class YouTubeUploadController {

    private final YouTubeUploadService youTubeUploadService;

    @Operation(
            summary = "YouTube에 동영상 업로드", // API 요약
            description = "제목과 내용을 받아 YouTube에 동영상을 업로드하는 API입니다." // API에 대한 설명
    )
    @PostMapping(value = "/{videoId}")
    @ResponseStatus(HttpStatus.CREATED) // 업로드가 완료되면 201 상태 코드 반환
    public String uploadVideo(
            @Parameter(description = "업로드할 동영상의 ID", required = true)
            @PathVariable("videoId") String videoId, // URL 경로로 받는 videoId 파라미터
            @Parameter(description = "동영상 제목", required = true)
            @RequestParam("title") String title, // 요청 파라미터로 받는 title
            @Parameter(description = "동영상 내용", required = true)
            @RequestParam("description") String description, // 요청 파라미터로 받는 description
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) { // OAuth2 인증 정보

        return youTubeUploadService.uploadVideo(videoId, authorizedClient.getAccessToken(), title, description);
    }
}
