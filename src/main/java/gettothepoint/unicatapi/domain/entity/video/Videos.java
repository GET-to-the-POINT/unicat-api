package gettothepoint.unicatapi.domain.entity.video;

import gettothepoint.unicatapi.domain.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
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

    @ManyToOne// Member와 다대일 관계 설정
    @JoinColumn(name = "member_id", nullable = false)  // 외래 키 컬럼명 설정
    private Member member;  // 외래 키로 연결할 Member 엔티티

    @PrePersist
    protected void onCreate() {
        this.createAt = Instant.now();
    }
}
