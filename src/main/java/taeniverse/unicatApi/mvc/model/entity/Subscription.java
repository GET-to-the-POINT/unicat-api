package taeniverse.unicatApi.mvc.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Entity
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private LocalDateTime endDate;

    @Column(nullable = false)
    @Setter
    private String status; // 구독 상태 (pending,active, cancel, expired)

    @ManyToOne
    @JoinColumn
    private Member member; // 한 유저는 하나의 구독만 가능

    @OneToOne
    private Order order; // 구독은 하나의 주문과 연결

    @Builder
    public Subscription(LocalDateTime endDate, Member member, Order order) {
        this.endDate = endDate;
        this.status = "Active";
        this.member = member;
        this.order = order;
        this.endDate = LocalDateTime.now().plusMonths(1);
    }
}