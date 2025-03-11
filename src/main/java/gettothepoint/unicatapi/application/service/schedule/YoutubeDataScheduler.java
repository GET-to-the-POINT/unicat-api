package gettothepoint.unicatapi.application.service.schedule;

import gettothepoint.unicatapi.application.service.video.VideoDataUpdateService;
import gettothepoint.unicatapi.domain.repository.video.YouTubeUploadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class YoutubeDataScheduler {

    @Autowired
    private VideoDataUpdateService videoDataUpdateService;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    private YouTubeUploadRepository youTubeUploadRepository; // 업로드된 동영상 목록을 조회하기 위한 repository

    /**
     * 매일 새벽 3시에 실행되는 스케줄러
     * upload_video 테이블에 있는 모든 동영상을 처리
     */
    @Scheduled(cron = "0 * * * * ?")
    public void updateYoutubeData() {
        System.out.println("-- 유튜브 데이터 업데이트 시작 --");

        // upload_video 테이블에서 모든 동영상 목록을 조회
        List<Long> videoIds = youTubeUploadRepository.findAllVideoIds(); // 동영상 ID 리스트를 가져옴
        for (Long videoId : videoIds) {
            try {
                // 해당 동영상의 주인 ID를 가져옴
                Long memberId = youTubeUploadRepository.findOwnerByVideoId(videoId);

                // 해당 회원의 액세스 토큰을 가져옴
                OAuth2AccessToken accessToken = getAccessTokenFromAuthorizedClient(memberId);

                // 액세스 토큰을 이용하여 유튜브 API에 업로드 작업 수행
                videoDataUpdateService.updateAllVideos(accessToken);

            } catch (Exception e) {
                System.err.println("유튜브 데이터 업데이트 중 오류 발생 (비디오 ID: " + videoId + "): " + e.getMessage());
            }
        }

        System.out.println("-- 유튜브 데이터 업데이트 완료 --");
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

























//
//
//
//package gettothepoint.unicatapi.application.service.schedule;
//
//import gettothepoint.unicatapi.infrastructure.security.youtube.YoutubeOAuth2Service;
//import lombok.RequiredArgsConstructor;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.security.oauth2.core.OAuth2AccessToken;
//import org.springframework.stereotype.Component;
//import gettothepoint.unicatapi.application.service.video.VideoDataUpdateService;
//
//import java.util.Collections;
//import java.util.List;
//
//@RequiredArgsConstructor
//@Component
//public class YoutubeDataScheduler {
//
//    private final VideoDataUpdateService videoDataUpdateService;
//    private final YoutubeOAuth2Service youtubeOAuth2Service;
//
//    @Scheduled(cron = "0 * * * * ?")
//    public void updateYoutubeData() {
//        System.out.println("-- 유튜브 데이터 업데이트 시작 --");
//        try {
//            OAuth2AccessToken accessToken = getAccessTokenFromExternalSource();
//            videoDataUpdateService.updateAllVideos(accessToken);
//        } catch (Exception e) {
//            System.err.println("유튜브 데이터 업데이트 중 오류 발생: " + e.getMessage());
//        }
//        System.out.println("-- 유튜브 데이터 업데이트 완료 --");
//    }
//
//
//
//}