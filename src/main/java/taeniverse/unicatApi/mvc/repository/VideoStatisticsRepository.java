package taeniverse.unicatApi.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import taeniverse.unicatApi.mvc.model.entity.VideoStatistics;
import java.util.Date;
import java.util.List;

@Repository
public interface VideoStatisticsRepository extends JpaRepository<VideoStatistics, Long> {
    // 특정 비디오와 기간 사이의 통계 조회
    List<VideoStatistics> findByVideoIdAndTimestampBetween(String videoId, Date startDate, Date endDate);
}
