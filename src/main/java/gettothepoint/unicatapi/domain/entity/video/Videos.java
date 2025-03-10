package gettothepoint.unicatapi.domain.entity.video;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Getter
@Entity
@Setter
public class Videos {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "video_seq")
    @Column(name = "video_id")
    private Long videoId;

    private String filePath;
    private String title;
    private Instant createAt;

    @PrePersist
    protected void onCreate() {
        this.createAt = Instant.now();
    }
}
