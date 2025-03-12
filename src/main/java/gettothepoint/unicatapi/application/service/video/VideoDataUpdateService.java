package gettothepoint.unicatapi.application.service.video;

import gettothepoint.unicatapi.domain.entity.Project;
import gettothepoint.unicatapi.domain.entity.video.UploadVideo;
import gettothepoint.unicatapi.domain.entity.video.VideoHistory;
import gettothepoint.unicatapi.domain.repository.ProjectRepository;
import gettothepoint.unicatapi.domain.repository.video.VideoHistoryRepository;
import gettothepoint.unicatapi.domain.repository.video.UploadVideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional
public class VideoDataUpdateService {

    private final YoutubeDataService youtubeDataService;
    private final UploadVideoRepository uploadVideoRepository;
    private final VideoHistoryRepository videoHistoryRepository;
    private final ProjectRepository projectRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;

    // 모든 비디오 업데이트 수행
    public void updateAllVideos(Long memberId) {
        OAuth2AccessToken accessToken = getAccessTokenFromAuthorizedClient(memberId);
        List<Project> projects = projectRepository.findProjectsWithUploadVideoByMemberId(memberId);
        for (Project project : projects) {
            updateOrInsertVideoData(project.getUploadVideo().getLinkId(), accessToken);
        }
    }

    @Transactional
    public void updateOrInsertVideoData(String linkId, OAuth2AccessToken accessToken) {
        // 유튜브 API에서 통계 가져오기
        String statistics = youtubeDataService.getVideoData(linkId, accessToken);

        String[] parts = statistics.split(",");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid statistics format: " + statistics);
        }

        // 숫자만 추출하는 함수 사용
        BigInteger viewCount = extractNumber(parts[0]);
        BigInteger likeCount = extractNumber(parts[1]);
        BigInteger commentCount = extractNumber(parts[2]);

        Optional<UploadVideo> existingUploadVideo = uploadVideoRepository.findByLinkId(linkId);

        if (existingUploadVideo.isPresent()) {
            UploadVideo uploadVideo = existingUploadVideo.get();
            VideoHistory videoHistory = VideoHistory.builder()
                    .uploadVideo(uploadVideo)
                    .viewCount(viewCount)
                    .likeCount(likeCount)
                    .commentCount(commentCount)
                    .build();
            videoHistoryRepository.save(videoHistory);
        }
    }

    // 숫자만 추출하는 유틸리티 메서드
    private BigInteger extractNumber(String text) {
        return new BigInteger(text.replaceAll("[^0-9]", "")); // 숫자만 남기고 변환
    }


    /**
     * OAuth2AuthorizedClientService에서 액세스 토큰을 조회
     * @param memberId 회원의 ID
     * @return 해당 회원의 액세스 토큰
     */
    private OAuth2AccessToken getAccessTokenFromAuthorizedClient(Long memberId) {
        // memberId와 일치하는 클라이언트를 로드
        String principalName = memberId.toString();

        OAuth2AuthorizedClient authorizedClient = authorizedClientService
                .loadAuthorizedClient("google", principalName); // "youtube"는 클라이언트 ID

        if (authorizedClient == null) {
            throw new IllegalStateException("Access token not found for member " + memberId);
        }

        return authorizedClient.getAccessToken();
    }
}