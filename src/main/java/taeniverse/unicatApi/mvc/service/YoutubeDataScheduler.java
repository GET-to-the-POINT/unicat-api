package taeniverse.unicatApi.mvc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.security.GeneralSecurityException;

@Component  // 스프링 컴포넌트로 등록해서 스케줄링 기능이 자동으로 활성화되도록 합니다.
public class YoutubeDataScheduler {

    @Autowired
    private VideoStatisticsService videoStatisticsService;

    @Scheduled(cron = "0 0 4 * * ?")  // 매일 자정에 실행
    public void updateYoutubeData() {
        System.out.println("--유튜브 데이터 업데이트 시작--");
        try {
            // getVideoStatistics() 호출하여 유튜브 통계 업데이트
            String statistics = videoStatisticsService.getVideoStatistics();
            System.out.println(statistics);
        } catch (GeneralSecurityException | IOException e) {
            System.err.println("유튜브 데이터 업데이트 중 오류 발생: " + e.getMessage());
        }
        System.out.println("--유튜브 데이터 업데이트 완료--");
    }
}