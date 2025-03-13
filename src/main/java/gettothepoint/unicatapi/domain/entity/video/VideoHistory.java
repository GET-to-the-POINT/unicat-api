package gettothepoint.unicatapi.domain.entity.video;

import gettothepoint.unicatapi.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigInteger;

@Getter
@NoArgsConstructor
@Entity
public class VideoHistory extends BaseEntity {

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
}
