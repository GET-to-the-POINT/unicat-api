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
    private UploadVideo uploadVideo;

    private BigInteger viewCount;
    private BigInteger likeCount;
    private BigInteger commentCount;

    @Builder
    public VideoHistory(UploadVideo uploadVideo, BigInteger viewCount, BigInteger likeCount, BigInteger commentCount) {
        this.uploadVideo = uploadVideo;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
