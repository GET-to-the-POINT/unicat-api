package getToThePoint.unicatApi.application.service.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import getToThePoint.unicatApi.application.service.video.VideoUpdateService;

@RequiredArgsConstructor
@Component
public class YoutubeDataScheduler {

    private final VideoUpdateService videoUpdateService;

    @Scheduled(cron = "0 0 4 * * ?")
    public void updateYoutubeData() {
        System.out.println("-- 유튜브 데이터 업데이트 시작 --");
        try {
            videoUpdateService.updateAllVideos();
        } catch (Exception e) {
            System.err.println("유튜브 데이터 업데이트 중 오류 발생: " + e.getMessage());
        }
        System.out.println("-- 유튜브 데이터 업데이트 완료 --");
    }
}