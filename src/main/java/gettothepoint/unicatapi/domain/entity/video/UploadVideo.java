package gettothepoint.unicatapi.domain.entity.video;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@NoArgsConstructor
@Getter
@Entity
public class UploadVideo {

    @Id
    @Column(name = "youtube_video_id")
    private String youtubeVideoId;  // 기본 키로 사용

    @OneToOne
    @JoinColumn(name = "video_id", referencedColumnName = "video_id")
    private Videos video;

    private BigInteger viewCount;
    private BigInteger likeCount;
    private BigInteger commentCount;
    private LocalDate timestamp;

    @Column(name = "member_id")  // memberId 컬럼 추가
    private Long memberId;


    public UploadVideo(Videos video, LocalDateTime timestamp, String youtubeVideoId) {
        this.video = video;
        this.timestamp = timestamp.toLocalDate();
        this.youtubeVideoId = youtubeVideoId;
    }

    // Builder 방식으로 객체 생성
    @Builder
    public UploadVideo(Videos video, LocalDateTime timestamp, String youtubeVideoId, BigInteger viewCount, BigInteger likeCount, BigInteger commentCount , Long memberId) {
        this.video = video;
        this.timestamp = timestamp.toLocalDate();
        this.youtubeVideoId = youtubeVideoId;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.memberId = memberId;
    }
}