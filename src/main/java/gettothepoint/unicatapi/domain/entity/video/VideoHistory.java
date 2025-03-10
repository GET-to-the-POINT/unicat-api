package gettothepoint.unicatapi.domain.entity.video;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Data
@Entity
public class VideoHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 📌 자동 증가 PK
    private Long id;

    @ManyToOne
    @JoinColumn(name = "youtube_video_id", referencedColumnName = "youtube_video_id", nullable = false)
    private UploadVideo uploadVideo;

    private LocalDateTime updateDate;
    private BigInteger viewCount;
    private BigInteger likeCount;
    private BigInteger commentCount;

    @Builder
    public VideoHistory(UploadVideo uploadVideo, LocalDateTime updateDate, BigInteger viewCount, BigInteger likeCount, BigInteger commentCount) {
        this.uploadVideo = uploadVideo;
        this.updateDate = updateDate;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }
}
