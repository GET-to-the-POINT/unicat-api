package gettothepoint.unicatapi.subscription.domain.entity;

import gettothepoint.unicatapi.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 구독 플랜 정보를 나타내는 엔티티
 * 구독 플랜의 이름, 설명, 가격 및 서비스 제공량 등의 정보를 관리합니다.
 */
@NoArgsConstructor
@Getter
@Entity
public class Plan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;

    private String description;

    private Long price;

    private Long aiImageCount;

    private Long aiScriptCount;

    private Long artifactCount;

    @OneToMany(mappedBy = "plan")
    private List<Subscription> subscription;

    @Builder
    public Plan(String name, String description, Long price, Long aiImageCount, Long aiScriptCount, Long artifactCount) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.aiImageCount = aiImageCount;
        this.aiScriptCount = aiScriptCount;
        this.artifactCount = artifactCount;
    }
    
    /**
     * 현재 플랜이 기본(BASIC) 플랜인지 확인
     * 
     * @return 기본 플랜 여부
     */
    public boolean isBasicPlan() {
        return "BASIC".equals(this.name);
    }
    
    /**
     * 현재 플랜이 프리미엄(PREMIUM) 플랜인지 확인
     * 
     * @return 프리미엄 플랜 여부
     */
    public boolean isPremiumPlan() {
        return "PREMIUM".equals(this.name);
    }
}
