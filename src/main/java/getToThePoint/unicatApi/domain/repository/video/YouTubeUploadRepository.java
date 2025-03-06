package getToThePoint.unicatApi.domain.repository.video;

import getToThePoint.unicatApi.domain.entity.video.UploadVideo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface YouTubeUploadRepository extends JpaRepository<UploadVideo, Long> {

}