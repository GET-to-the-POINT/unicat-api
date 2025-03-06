package taeniverse.unicatApi.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import taeniverse.unicatApi.mvc.model.entity.UploadVideo;

public interface YouTubeUploadRepository extends JpaRepository<UploadVideo, Long> {

}