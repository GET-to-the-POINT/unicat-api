package taeniverse.unicatApi.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import taeniverse.unicatApi.mvc.service.YoutubeDataService;


@RequiredArgsConstructor
//통계로 가져올수 있는건 조회수, 좋아요수, 댓글수
@RestController
public class YoutubeDataController {

    private final YoutubeDataService youtubeDataService;

    // YouTube 동영상 통계 반환 API
    @GetMapping("/api/video-statistics/{videoId}")
    public String getVideoStatistics(@RequestParam String[] videoIds) {
        try {
            return youtubeDataService.getVideosData(videoIds);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
