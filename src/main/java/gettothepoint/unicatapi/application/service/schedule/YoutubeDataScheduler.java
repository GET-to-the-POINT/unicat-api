package gettothepoint.unicatapi.application.service.schedule;

import gettothepoint.unicatapi.application.service.video.VideoDataUpdateService;
import gettothepoint.unicatapi.domain.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class YoutubeDataScheduler {

    private final VideoDataUpdateService videoDataUpdateService;
    private final ProjectRepository projectRepository;

    @Scheduled(cron = "0 0 5 * * ?")
    public void updateYoutubeData() {
        List<Long> memberIds = projectRepository.findDistinctMemberIdsByUploadVideoIsNotNull();

        for (Long memberId : memberIds) {
            videoDataUpdateService.updateAllVideos(memberId);
        }
    }
}