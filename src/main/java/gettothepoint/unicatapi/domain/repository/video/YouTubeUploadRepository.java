package gettothepoint.unicatapi.domain.repository.video;

import gettothepoint.unicatapi.domain.entity.video.UploadVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface YouTubeUploadRepository extends JpaRepository<UploadVideo, Long> {

    @Query("SELECT v.video.videoId FROM UploadVideo v")
    List<Long> findAllVideoIds(); // 모든 동영상 ID 조회


    @Query("SELECT v.video.member.id FROM UploadVideo v WHERE v.video.videoId = :videoId")
    Long findOwnerByVideoId(@Param("videoId") Long videoId); // 특정 동영상의 주인 ID 조회


}