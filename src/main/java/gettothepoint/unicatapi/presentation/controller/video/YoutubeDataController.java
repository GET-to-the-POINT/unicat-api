package gettothepoint.unicatapi.presentation.controller.video;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import gettothepoint.unicatapi.application.service.video.YoutubeDataService;


@RequiredArgsConstructor
//통계로 가져올수 있는건 조회수, 좋아요수, 댓글수
@RestController
public class YoutubeDataController {

    private final YoutubeDataService youtubeDataService;

    // YouTube 동영상 통계 반환 API
    @GetMapping("/api/video-statistics/{videoId}")
    public String getVideoStatisticsEntity(@RequestParam String[] videoIds, @AuthenticationPrincipal OAuth2AuthenticationToken authentication) {
        try {
            OAuth2AccessToken accessToken = ((OAuth2AuthenticationToken) authentication).getPrincipal().getAttribute("access_token");
            return youtubeDataService.getVideosData(videoIds, accessToken);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
