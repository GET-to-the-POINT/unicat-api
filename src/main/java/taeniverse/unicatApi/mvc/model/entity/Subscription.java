package taeniverse.unicatApi.mvc.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member; // 한 유저는 하나의 구독만 가능

    @OneToOne(mappedBy = "subscription", cascade = CascadeType.ALL)
    private Order order; // 구독은 하나의 주문과 연결

    @Column(nullable = false)
    @Builder.Default
    private String status = "pending"; // 구독 상태 (pending,active, cancel, expired)

    private LocalDateTime endDate;

    // 생성 및 수정 시간을 직접 관리하는 필드 추가
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.endDate == null) {
            this.endDate = now.plusMonths(1);
        }
        this.createdAt = now;
        this.updatedAt = now;
    }
    @PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}