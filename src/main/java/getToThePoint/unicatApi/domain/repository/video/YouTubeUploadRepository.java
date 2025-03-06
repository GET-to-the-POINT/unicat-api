package getToThePoint.unicatApi.domain.repository.video;

import org.springframework.data.jpa.repository.JpaRepository;
import getToThePoint.unicatApi.domain.entity.video.UploadVideo;

public interface YouTubeUploadRepository extends JpaRepository<UploadVideo, Long> {

}