package gettothepoint.unicatapi.application.service.schedule;

import gettothepoint.unicatapi.application.service.video.VideoDataUpdateService;
import gettothepoint.unicatapi.domain.repository.ProjectRepository;
import gettothepoint.unicatapi.domain.repository.video.UploadVideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class YoutubeDataScheduler {

    private final VideoDataUpdateService videoDataUpdateService;
    private final ProjectRepository projectRepository;

    /**
     * 매일 새벽 3시에 실행되는 스케줄러
     * upload_video 테이블에 있는 모든 동영상을 처리
     */
    @Scheduled(cron = "0 0 5 * * ?")
    public void updateYoutubeData() {
        System.out.println("-- 유튜브 데이터 업데이트 시작 --");

        List<Long> memberIds = projectRepository.findDistinctMemberIdsByUploadVideoIsNotNull();

        for (Long memberId : memberIds) {
            // 액세스 토큰을 이용하여 유튜브 API에 업로드 작업 수행
            videoDataUpdateService.updateAllVideos(memberId);
        }

        System.out.println("-- 유튜브 데이터 업데이트 완료 --");
    }

}