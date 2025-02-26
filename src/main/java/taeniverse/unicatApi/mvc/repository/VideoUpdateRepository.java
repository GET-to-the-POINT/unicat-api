package taeniverse.unicatApi.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import taeniverse.unicatApi.mvc.model.entity.VideoStatistics;
import java.util.List;

public interface VideoUpdateRepository extends JpaRepository<VideoStatistics, Long> {
    @Query(value = "SELECT video_id FROM you_tube_video", nativeQuery = true)
    List<String> findAllVideoIds();
}