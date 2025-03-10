package gettothepoint.unicatapi.application.service.video;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import gettothepoint.unicatapi.domain.entity.video.VideoHistory;
import gettothepoint.unicatapi.domain.repository.video.VideoHistoryRepository;
import gettothepoint.unicatapi.domain.repository.video.VideoUpdateRepository;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Service
public class VideoStatisticsEntityService {

    private final VideoUpdateRepository videoUpdateRepository;
    private final VideoHistoryRepository VideoStatisticsEntityRepository;

    // 특정 비디오에 대한 특정 기간의 통계 계산
    public String getStatisticsForVideo(String youtubevideoId, Date startdate, Date enddate) {
        // 해당 비디오와 기간에 맞는 데이터 조회
        List<VideoHistory> statisticsList = VideoStatisticsEntityRepository.findByUploadVideo_YoutubeVideoIdAndUpdateDateBetween(youtubevideoId, startdate, enddate);

        if (statisticsList.isEmpty()) {
            return "해당 비디오와 기간에 대한 통계 데이터가 없습니다.";
        }

        // 각 통계 값 계산
        BigInteger totalViewCount = BigInteger.ZERO;
        BigInteger totalLikeCount = BigInteger.ZERO;
        BigInteger totalCommentCount = BigInteger.ZERO;

        BigInteger maxViewCount = BigInteger.ZERO;
        BigInteger minViewCount = BigInteger.valueOf(Long.MAX_VALUE);

        BigInteger maxLikeCount = BigInteger.ZERO;
        BigInteger minLikeCount = BigInteger.valueOf(Long.MAX_VALUE);

        // 통계 계산
        for (VideoHistory videoStat : statisticsList) {
            totalViewCount = totalViewCount.add(videoStat.getViewCount());
            totalLikeCount = totalLikeCount.add(videoStat.getLikeCount());
            totalCommentCount = totalCommentCount.add(videoStat.getCommentCount());

            // 최대/최소 조회수
            if (videoStat.getViewCount().compareTo(maxViewCount) > 0) {
                maxViewCount = videoStat.getViewCount();
            }
            if (videoStat.getViewCount().compareTo(minViewCount) < 0) {
                minViewCount = videoStat.getViewCount();
            }

            // 최대/최소 좋아요 수
            if (videoStat.getLikeCount().compareTo(maxLikeCount) > 0) {
                maxLikeCount = videoStat.getLikeCount();
            }
            if (videoStat.getLikeCount().compareTo(minLikeCount) < 0) {
                minLikeCount = videoStat.getLikeCount();
            }
        }

        // 평균 조회수, 좋아요 수 계산
        BigInteger averageViewCount = totalViewCount.divide(BigInteger.valueOf(statisticsList.size()));
        BigInteger averageLikeCount = totalLikeCount.divide(BigInteger.valueOf(statisticsList.size()));
        BigInteger averageCommentCount = totalCommentCount.divide(BigInteger.valueOf(statisticsList.size()));

        // 결과 문자열 생성
        return String.format(
                "기간: %s ~ %s<br>" +
                        "비디오 ID: %s<br>" +
                        "평균 조회수: %d<br>" +
                        "평균 좋아요 수: %d<br>" +
                        "평균 댓글 수: %d<br>" +
                        "최대 조회수: %d<br>" +
                        "최저 조회수: %d<br>" +
                        "최대 좋아요 수: %d<br>" +
                        "최저 좋아요 수: %d<br>",
                startdate, enddate, youtubevideoId,
                averageViewCount, averageLikeCount, averageCommentCount,
                maxViewCount, minViewCount,
                maxLikeCount, minLikeCount
        );
    }
}
