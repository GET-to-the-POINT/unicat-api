package gettothepoint.unicatapi.application.service.video;

import gettothepoint.unicatapi.domain.entity.dashboard.Project;
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

    public void updateAllVideos(Long memberId) {
        OAuth2AccessToken accessToken = getAccessTokenFromAuthorizedClient(memberId);
        List<Project> projects = projectRepository.findProjectsWithUploadVideoByMemberId(memberId);
        for (Project project : projects) {
            updateOrInsertVideoData(project.getUploadVideo().getLinkId(), accessToken);
        }
    }

    @Transactional
    public void updateOrInsertVideoData(String linkId, OAuth2AccessToken accessToken) {
        String statistics = youtubeDataService.getVideoData(linkId, accessToken);
        String[] parts = statistics.split(",");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid statistics format: " + statistics);
        }

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

    private BigInteger extractNumber(String text) {
        return new BigInteger(text.replaceAll("\\D", ""));
    }

    private OAuth2AccessToken getAccessTokenFromAuthorizedClient(Long memberId) {
        String principalName = memberId.toString();

        OAuth2AuthorizedClient authorizedClient = authorizedClientService
                .loadAuthorizedClient("google", principalName);

        if (authorizedClient == null) {
            throw new IllegalStateException("Access token not found for member " + memberId);
        }

        return authorizedClient.getAccessToken();
    }
}