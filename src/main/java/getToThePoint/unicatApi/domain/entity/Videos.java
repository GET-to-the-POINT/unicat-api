package taeniverse.unicatApi.mvc.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.Instant;

@Getter
@Entity
public class Videos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long videoId;

    private String filePath;
    private String title;

    private Instant createAt;

    @PrePersist
    protected void onCreate() {
        this.createAt = Instant.now();
    }
}
