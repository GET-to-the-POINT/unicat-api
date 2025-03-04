package taeniverse.unicatApi.mvc.model.entity;

import jakarta.persistence.*;

import lombok.Getter;
import org.springframework.data.jpa.repository.Temporal;
import java.time.LocalDateTime;

@Getter
@Entity
public class UploadVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String videoId;  // YouTube API에서 받은 동영상 ID
    private String title;
    private String description;
    private String filePath;  // 동영상 파일 경로
    private LocalDateTime uploadDate;
    private String privacyStatus;  // "public", "private", "unlisted"

}
