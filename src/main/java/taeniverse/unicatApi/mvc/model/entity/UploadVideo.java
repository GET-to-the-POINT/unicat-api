package taeniverse.unicatApi.mvc.model.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

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

    private LocalDate updateScheduleDate;

    @Builder
    public UploadVideo(Videos video, LocalDate updateScheduleDate) {
        this.video = video;
        this.updateScheduleDate = updateScheduleDate;
    }
}