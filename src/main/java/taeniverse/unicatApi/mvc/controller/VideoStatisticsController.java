package taeniverse.unicatApi.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import taeniverse.unicatApi.mvc.service.VideoStatisticsService;
import java.util.Date;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/statistics")
public class VideoStatisticsController {

    private final VideoStatisticsService videoStatisticsService;

    // 특정 비디오에 대한 (특정 기간동안) 통계 요청
    @GetMapping("/video/{videoId}/period")
    public String getStatisticsForVideo(
            @PathVariable String videoId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return videoStatisticsService.getStatisticsForVideo(videoId, startDate, endDate);
    }
}
