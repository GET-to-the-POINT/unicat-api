package taeniverse.unicatApi.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import taeniverse.unicatApi.mvc.model.entity.UploadVideo;


public interface YouTubeRepository extends JpaRepository<UploadVideo, Long> {
    // videoId로 동영상을 찾는 메서드
    UploadVideo findByVideoId(String videoId);
}