package gettothepoint.unicatapi.domain.repository.video;

import gettothepoint.unicatapi.domain.entity.video.UploadVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UploadVideoRepository extends JpaRepository<UploadVideo, Long> {
    Optional<UploadVideo> findByLinkId(String linkId);
}