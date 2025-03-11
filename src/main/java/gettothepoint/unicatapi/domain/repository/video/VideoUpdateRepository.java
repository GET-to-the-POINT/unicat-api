package gettothepoint.unicatapi.domain.repository.video;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import gettothepoint.unicatapi.domain.entity.video.UploadVideo;

import java.util.List;
import java.util.Optional;

public interface VideoUpdateRepository extends JpaRepository<UploadVideo, Long> {
    @Query(value = "SELECT youtube_video_id FROM upload_video", nativeQuery = true)
    List<String> findAllVideoIds();
    Optional<UploadVideo> findFirstByYoutubeVideoId(String youtubeVideoId);
}