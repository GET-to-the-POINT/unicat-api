package taeniverse.unicatApi.mvc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import taeniverse.unicatApi.mvc.service.VideoStatisticsService;



@RestController
public class VideoStatisticsController {

    private final VideoStatisticsService videoStatisticsService;

    @Autowired
    public VideoStatisticsController(VideoStatisticsService videoStatisticsService) {
        this.videoStatisticsService = videoStatisticsService;
    }

    // YouTube 동영상 통계 반환 API
    @GetMapping("/api/video-statistics")
    public String getVideoStatistics() {
        try {
            return videoStatisticsService.getVideoStatistics();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}