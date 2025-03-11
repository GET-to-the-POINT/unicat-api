package gettothepoint.unicatapi.presentation.controller.video;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import gettothepoint.unicatapi.application.service.video.VideoStatisticsEntityService;

import java.util.Date;

@RequiredArgsConstructor
@RestController
@RequestMapping("/statistics")
@Tag(name = "Video Statistics", description = "비디오 통계 관련 API")
public class VideoStatisticsEntityController {

    private final VideoStatisticsEntityService VideoStatisticsEntityService;

    @Operation(
            summary = "특정 비디오에 대한 통계 조회", // API 요약
            description = "특정 비디오에 대해 지정된 기간 동안의 통계를 조회하는 API입니다." // API에 대한 설명
    )

    // 특정 비디오에 대한 (특정 기간동안) 통계 요청
    @GetMapping("/{videoId}/period")
    public String getStatisticsForVideo(
            @Parameter(description = "조회할 비디오의 ID", required = true)
            @PathVariable String videoId,
            @Parameter(description = "통계 조회 시작 날짜 (yyyy-MM-dd 형식)", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startdate,
            @Parameter(description = "통계 조회 종료 날짜 (yyyy-MM-dd 형식)", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd")  Date enddate) {
        return VideoStatisticsEntityService.getStatisticsForVideo(videoId, startdate, enddate);
    }
}
