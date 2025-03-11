package gettothepoint.unicatapi.domain.repository.video;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import gettothepoint.unicatapi.domain.entity.video.VideoHistory;
import java.util.Date;
import java.util.List;

@Repository
public interface VideoHistoryRepository extends JpaRepository<VideoHistory, Long> {
    // 특정 비디오와 기간 사이의 통계 조회
    List<VideoHistory> findByUploadVideo_YoutubeVideoIdAndUpdateDateBetween(String youtubeVideoId, Date startDate, Date endDate);
}
