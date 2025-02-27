//package taeniverse.unicatApi.mvc.repository;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import java.util.List;
//
//import org.springframework.data.jpa.repository.Query;
//import taeniverse.unicatApi.mvc.model.entity.YoutubeVideo;
//
//public interface YouTubeAnalyticsRepository extends JpaRepository<YoutubeVideo, Long> {
//
//    // 동영상 ID만 가져오는 쿼리
//    @Query("SELECT y.videoId FROM YoutubeVideo y")
//    List<Long> findAllIds();  // 동영상의 ID만 가져오기
//}
