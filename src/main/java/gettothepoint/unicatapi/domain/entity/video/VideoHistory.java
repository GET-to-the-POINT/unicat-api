package gettothepoint.unicatapi.domain.entity.video;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import java.math.BigInteger;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Data
@Entity
public class VideoHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "youtube_video_id", referencedColumnName = "youtube_video_id", nullable = false)
    private UploadVideo uploadVideo;

    private LocalDateTime updateDate;
    private BigInteger viewCount;
    private BigInteger likeCount;
    private BigInteger commentCount;

    @Column(name = "member_id")  // memberId 컬럼 추가
    private Long memberId;

    @Builder
    public VideoHistory(UploadVideo uploadVideo, LocalDateTime updateDate, BigInteger viewCount, BigInteger likeCount, BigInteger commentCount ,Long memberId) {
        this.uploadVideo = uploadVideo;
        this.updateDate = updateDate;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.memberId = memberId;
    }
}
