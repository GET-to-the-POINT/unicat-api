package gettothepoint.unicatapi.domain.repository.video;

import gettothepoint.unicatapi.domain.entity.video.Videos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideosRepository extends JpaRepository<Videos, Long> {
    Optional<Videos> findByVideoId(Long videoId);

}