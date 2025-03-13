package gettothepoint.unicatapi.application.service.video;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import gettothepoint.unicatapi.domain.entity.video.VideoHistory;
import gettothepoint.unicatapi.domain.repository.video.VideoHistoryRepository;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class VideoStatisticsEntityService {

    private final VideoHistoryRepository videoHistoryRepository;

    public String getStatisticsForVideo(String linkId, LocalDateTime startDate, LocalDateTime endDate) {
        List<VideoHistory> statisticsList = videoHistoryRepository.findByUploadVideo_LinkIdAndUpdatedAtBetween(linkId, startDate, endDate);

        if (statisticsList.isEmpty()) {
            return "해당 비디오와 기간에 대한 통계 데이터가 없습니다.";
        }

        BigInteger totalViewCount = BigInteger.ZERO;
        BigInteger totalLikeCount = BigInteger.ZERO;
        BigInteger totalCommentCount = BigInteger.ZERO;

        BigInteger maxViewCount = BigInteger.ZERO;
        BigInteger minViewCount = BigInteger.valueOf(Long.MAX_VALUE);

        BigInteger maxLikeCount = BigInteger.ZERO;
        BigInteger minLikeCount = BigInteger.valueOf(Long.MAX_VALUE);

        for (VideoHistory videoStat : statisticsList) {
            totalViewCount = totalViewCount.add(videoStat.getViewCount());
            totalLikeCount = totalLikeCount.add(videoStat.getLikeCount());
            totalCommentCount = totalCommentCount.add(videoStat.getCommentCount());

            if (videoStat.getViewCount().compareTo(maxViewCount) > 0) {
                maxViewCount = videoStat.getViewCount();
            }
            if (videoStat.getViewCount().compareTo(minViewCount) < 0) {
                minViewCount = videoStat.getViewCount();
            }

            if (videoStat.getLikeCount().compareTo(maxLikeCount) > 0) {
                maxLikeCount = videoStat.getLikeCount();
            }
            if (videoStat.getLikeCount().compareTo(minLikeCount) < 0) {
                minLikeCount = videoStat.getLikeCount();
            }
        }

        BigInteger averageViewCount = totalViewCount.divide(BigInteger.valueOf(statisticsList.size()));
        BigInteger averageLikeCount = totalLikeCount.divide(BigInteger.valueOf(statisticsList.size()));
        BigInteger averageCommentCount = totalCommentCount.divide(BigInteger.valueOf(statisticsList.size()));

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
                startDate, endDate, linkId,
                averageViewCount, averageLikeCount, averageCommentCount,
                maxViewCount, minViewCount,
                maxLikeCount, minLikeCount
        );
    }
}