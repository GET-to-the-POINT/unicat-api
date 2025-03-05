package taeniverse.unicatApi.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import taeniverse.unicatApi.mvc.model.entity.VideoStatistics;
import java.util.List;

public interface VideoUpdateRepository extends JpaRepository<VideoStatistics, Long> {
    @Query(value = "SELECT youtube_video_id FROM upload_video", nativeQuery = true)
    List<String> findAllVideoIds();
}