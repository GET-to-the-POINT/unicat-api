package getToThePoint.unicatApi.domain.repository.video;

import org.springframework.data.jpa.repository.JpaRepository;
import getToThePoint.unicatApi.domain.entity.video.Videos;
import java.util.Optional;

public interface VideosRepository extends JpaRepository<Videos, Long> {
    Optional<Videos> findByVideoId(Long videoId);
}