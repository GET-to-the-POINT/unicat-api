package taeniverse.unicatApi.mvc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import taeniverse.unicatApi.mvc.model.entity.UploadVideo;
import taeniverse.unicatApi.mvc.model.entity.Videos;
import taeniverse.unicatApi.mvc.model.entity.VideoStatistics;
import taeniverse.unicatApi.mvc.model.entity.YoutubeVideo;
import taeniverse.unicatApi.mvc.repository.VideoUpdateRepository;
import taeniverse.unicatApi.mvc.repository.VideosRepository;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class VideoUpdateService {

    private final YoutubeDataService youtubeDataService;
    private final VideoUpdateRepository videoUpdateRepositor;
    private final VideosRepository videosRepository;

    // 모든 비디오 업데이트 수행
    public void updateAllVideos() throws Exception {
        List<String> videoIds = videoUpdateRepositor.findAllVideoIds();
        for (String videoId : videoIds) {
            updateAndSaveVideoStatistics(videoId);
        }
    }

    // 특정 비디오의 통계를 업데이트하고 저장
    public void updateAndSaveVideoStatistics(String videoId) throws Exception {
        // 유튜브 API에서 통계 가져오기
        String statistics = youtubeDataService.getVideoData(videoId);

        // 통계 데이터 파싱
        BigInteger viewCount = new BigInteger(statistics.split(",")[0].split(":")[1].trim());
        BigInteger likeCount = new BigInteger(statistics.split(",")[1].split(":")[1].trim());
        BigInteger commentCount = new BigInteger(statistics.split(",")[2].split(":")[1].trim());


        Videos video = videosRepository.findByVideoId(videoId).orElseThrow(() -> new IllegalArgumentException("Video not found"));

        // VideoStatistics 엔티티 생성
        UploadVideo uploadVideo = UploadVideo.builder()
                .video(video)
                .viewCount(viewCount)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .build();

//        // LocalDate를 java.sql.Date로 변환
//        java.sql.Date sqlDate = java.sql.Date.valueOf(LocalDate.now());


        // 저장 (Repository를 직접 호출)
        videoUpdateRepositor.save(uploadVideo);
    }
}
