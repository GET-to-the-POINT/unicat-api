package gettothepoint.unicatapi.domain.repository.video;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import gettothepoint.unicatapi.domain.entity.video.VideoHistory;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VideoHistoryRepository extends JpaRepository<VideoHistory, Long> {
    List<VideoHistory> findByUploadVideo_LinkIdAndUpdatedAtBetween(String linkId, LocalDateTime start, LocalDateTime end);
}
