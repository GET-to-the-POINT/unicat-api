package taeniverse.unicatApi.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import taeniverse.unicatApi.mvc.model.entity.YoutubeVideo;

public interface YoutubeDataRepository extends JpaRepository<YoutubeVideo, Long> {
    boolean existsByVideoId(String videoId);
}
