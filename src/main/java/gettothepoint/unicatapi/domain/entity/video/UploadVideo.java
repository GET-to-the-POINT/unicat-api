package gettothepoint.unicatapi.domain.entity.video;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@NoArgsConstructor
@Getter
@Entity
public class UploadVideo {

    @Id
    private Long videoId;

    @OneToOne
    @MapsId
    @JoinColumn
    private Videos video;

    @Temporal(TemporalType.DATE)
    private LocalDate updateScheduleDate; // 업데이트 날짜

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime timestamp;

    private BigInteger viewCount;
    private BigInteger likeCount;
    private BigInteger commentCount;
    private String youtubeVideoId;


    public UploadVideo(Videos video, LocalDate updateScheduleDate, String youtubeVideoId) {
        this.video = video;
        this.updateScheduleDate = updateScheduleDate;
        this.youtubeVideoId = youtubeVideoId;
    }

    @Builder
    public UploadVideo(Videos video, LocalDateTime timestamp, LocalDate updateScheduleDate, String youtubeVideoId, BigInteger viewCount, BigInteger likeCount, BigInteger commentCount) {
        this.video = video;
        this.timestamp = timestamp;
        this.updateScheduleDate = updateScheduleDate;
        this.youtubeVideoId = youtubeVideoId;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }

}