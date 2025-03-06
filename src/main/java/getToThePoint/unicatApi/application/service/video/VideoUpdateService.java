package getToThePoint.unicatApi.application.service.video;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import getToThePoint.unicatApi.domain.entity.video.UploadVideo;

import getToThePoint.unicatApi.domain.repository.video.VideoUpdateRepository;
import getToThePoint.unicatApi.domain.repository.video.VideosRepository;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class VideoUpdateService {

    private final YoutubeDataService youtubeDataService;
    private final VideoUpdateRepository videoUpdateRepositor;
    private final VideosRepository videosRepository;
    private final VideoUpdateRepository uploadVideoRepository;

    // 모든 비디오 업데이트 수행
    public void updateAllVideos() throws Exception {
        List<String> youtubeVideoIds = videoUpdateRepositor.findAllVideoIds();
        for (String youtubeVideoId : youtubeVideoIds) {
            updateAndSaveVideoStatisticsEntity(youtubeVideoId);
        }
    }

    // 특정 비디오의 통계를 업데이트하고 저장
    public void updateAndSaveVideoStatisticsEntity(String youtubeVideoId) throws Exception {
        // 유튜브 API에서 통계 가져오기
        String statistics = youtubeDataService.getVideoData(youtubeVideoId);

        // 통계 데이터 파싱
        String parsedYoutubeVideoId = statistics.split(",")[0].split(":")[1].trim().replace("\"", ""); // 유튜브 비디오 ID 추출
        BigInteger viewCount = new BigInteger(statistics.split(",")[0].split(":")[1].trim());
        BigInteger likeCount = new BigInteger(statistics.split(",")[1].split(":")[1].trim());
        BigInteger commentCount = new BigInteger(statistics.split(",")[2].split(":")[1].trim());

        // UploadVideo에서 관리하는 youtubeVideoId로 조회 (기존의 videoId가 아니라 youtubeVideoId로 조회)
        UploadVideo uploadVideo = videoUpdateRepositor.findByYoutubeVideoId(youtubeVideoId)
                .orElseThrow(() -> new IllegalArgumentException("UploadVideo not found"));

        LocalDate updateScheduleDate = LocalDate.now();

        // 셋터 방식으로 UploadVideo 객체 생성
        uploadVideo.setUpdateScheduleDate(updateScheduleDate); // updateScheduleDate 값 설정
        uploadVideo.setYoutubeVideoId(parsedYoutubeVideoId);  // youtubeVideoId 값 설정
        uploadVideo.setViewCount(viewCount);                // viewCount 값 설정
        uploadVideo.setLikeCount(likeCount);                // likeCount 값 설정
        uploadVideo.setCommentCount(commentCount);          // commentCount 값 설정

        // 저장 (Repository를 직접 호출)
        videoUpdateRepositor.save(uploadVideo);
    }
}