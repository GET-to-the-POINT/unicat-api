package gettothepoint.unicatapi.presentation.controller.video;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import gettothepoint.unicatapi.application.service.video.VideoStatisticsEntityService;
import java.time.LocalDateTime;
import java.util.regex.Matcher;

@RequiredArgsConstructor
@RestController
@RequestMapping("/statistics")
@Tag(name = "Video Statistics", description = "비디오 통계 관련 API")
public class VideoStatisticsEntityController {

    private final VideoStatisticsEntityService videoStatisticsEntityService;
    private static final String VIDEO_ID_REGEX = "^[a-zA-Z0-9_-]{11}$";

    @Operation(
            summary = "특정 비디오에 대한 통계 조회", // API 요약
            description = "특정 비디오에 대해 지정된 기간 동안의 통계를 조회하는 API입니다." // API에 대한 설명
    )

    // 특정 비디오에 대한 (특정 기간동안) 통계 요청
    @GetMapping("/{videoId}/period")
    public String getStatisticsForVideo(
            @Parameter(description = "조회할 비디오의 ID")
            @PathVariable String videoId,
            @Parameter(description = "통계 조회 시작 날짜 및 시간 (yyyy-MM-dd'T'HH:mm:ss 형식)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startDateTime,
            @Parameter(description = "통계 조회 종료 날짜 및 시간 (yyyy-MM-dd'T'HH:mm:ss 형식)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endDateTime) {

        if (startDateTime.isAfter(endDateTime)) {
            throw new IllegalArgumentException("Start time cannot be after end time.");
        }
        return videoStatisticsEntityService.getStatisticsForVideo(videoId, startDateTime, endDateTime);
    }
}
