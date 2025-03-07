package gettothepoint.unicatapi.domain.repository.video;

import gettothepoint.unicatapi.domain.entity.video.UploadVideo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface YouTubeUploadRepository extends JpaRepository<UploadVideo, Long> {

}