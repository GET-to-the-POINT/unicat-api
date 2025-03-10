package gettothepoint.unicatapi.presentation.controller.video;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import gettothepoint.unicatapi.application.service.video.VideoStatisticsEntityService;

import java.util.Date;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/statistics")
public class VideoStatisticsEntityController {

    private final VideoStatisticsEntityService VideoStatisticsEntityService;

    // 특정 비디오에 대한 (특정 기간동안) 통계 요청
    @GetMapping("/video/{videoId}/period")
    public String getStatisticsForVideo(
            @PathVariable String videoId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startdate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd")  Date enddate) {
        return VideoStatisticsEntityService.getStatisticsForVideo(videoId, startdate, enddate);
    }
}
