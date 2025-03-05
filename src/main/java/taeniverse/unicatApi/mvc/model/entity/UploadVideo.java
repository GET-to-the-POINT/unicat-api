package taeniverse.unicatApi.mvc.model.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Date;

@NoArgsConstructor
@Getter
@Entity
public class UploadVideo {

    @Id
    private Long videoId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "video_id")
    private Videos video;

    @Temporal(TemporalType.DATE)
    private LocalDate updateScheduleDate;

    private BigInteger viewCount;
    private BigInteger likeCount;
    private BigInteger commentCount;

    @Builder
    public UploadVideo(Videos video, LocalDate updateScheduleDate) {
        this.video = video;
        this.updateScheduleDate = updateScheduleDate;
    }
}