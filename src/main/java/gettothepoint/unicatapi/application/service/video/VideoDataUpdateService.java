package gettothepoint.unicatapi.application.service.video;

import gettothepoint.unicatapi.domain.entity.video.UploadVideo;
import gettothepoint.unicatapi.domain.entity.video.VideoHistory;
import gettothepoint.unicatapi.domain.repository.video.VideoHistoryRepository;
import gettothepoint.unicatapi.domain.repository.video.VideoUpdateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional
public class VideoDataUpdateService {

    private final YoutubeDataService youtubeDataService;
    private final VideoUpdateRepository videoUpdateRepository;
    private final VideoHistoryRepository videoHistoryRepository;

    // 모든 비디오 업데이트 수행
    public void updateAllVideos(OAuth2AccessToken accessToken) throws Exception {
        List<String> youtubeVideoIds = videoUpdateRepository.findAllVideoIds();
        for (String youtubeVideoId : youtubeVideoIds) {
            updateOrInsertVideoData(youtubeVideoId , accessToken);
        }
    }

    // 기존 업로드 비디오가 존재하면 업데이트 없이 `VideoHistory`에만 저장
    @Transactional
    public void updateOrInsertVideoData(String youtubeVideoId, OAuth2AccessToken accessToken) throws Exception {
        // 유튜브 API에서 통계 가져오기
        String statistics = youtubeDataService.getVideoData(youtubeVideoId, accessToken);

        String[] parts = statistics.split(",");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid statistics format: " + statistics);
        }

        // 숫자만 추출하는 함수 사용
        BigInteger viewCount = extractNumber(parts[0]);
        BigInteger likeCount = extractNumber(parts[1]);
        BigInteger commentCount = extractNumber(parts[2]);

        Optional<UploadVideo> existingUploadVideo = videoUpdateRepository.findFirstByYoutubeVideoId(youtubeVideoId);

        if (existingUploadVideo.isPresent()) {

           UploadVideo uploadVideo = existingUploadVideo.get();

            VideoHistory videoHistory = VideoHistory.builder()
                    .uploadVideo(uploadVideo)
                    .viewCount(viewCount)
                    .likeCount(likeCount)
                    .commentCount(commentCount)
                    .updateDate(LocalDateTime.now())
                    .memberId(uploadVideo.getMemberId())
                    .build();

            videoHistoryRepository.save(videoHistory);
        }
    }

    // 숫자만 추출하는 유틸리티 메서드
    private BigInteger extractNumber(String text) {
        return new BigInteger(text.replaceAll("[^0-9]", "")); // 숫자만 남기고 변환
    }
}