package gettothepoint.unicatapi.presentation.controller.video;

import gettothepoint.unicatapi.application.service.video.YoutubeUploadService;
import gettothepoint.unicatapi.domain.dto.video.VideoUploadRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/videos")
public class YouTubeUploadController {

    private final YoutubeUploadService youtubeUploadService;

    @Operation(summary = "YouTube에 동영상 업로드", description = "제목과 내용을 받아 YouTube에 동영상을 업로드하는 API입니다.")
    @PostMapping("/{videoId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void uploadVideo(
            @Parameter(description = "업로드할 동영상의 ID", required = true)
            @PathVariable("videoId") String videoId,
            @Valid @RequestBody VideoUploadRequest request,
            @Parameter(hidden = true)
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {

        youtubeUploadService.uploadVideo(
                videoId,
                authorizedClient.getAccessToken(),
                request.getTitle(),
                request.getDescription(),
                request.getVisibility()
        );
    }

}
