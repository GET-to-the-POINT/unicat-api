package gettothepoint.unicatapi.domain.repository.video;

import gettothepoint.unicatapi.domain.entity.video.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideosRepository extends JpaRepository<Video, Long> {
    Optional<Video> findByVideoId(Long videoId);

}