package gettothepoint.unicatapi.domain.repository.video;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import gettothepoint.unicatapi.domain.entity.video.VideoStatisticsEntity;
import java.util.Date;
import java.util.List;

@Repository
public interface VideoStatisticsEntityRepository extends JpaRepository<VideoStatisticsEntity, Long> {
    // 특정 비디오와 기간 사이의 통계 조회
    List<VideoStatisticsEntity> findByVideoIdAndTimestampBetween(String videoId, Date startDate, Date endDate);
}
