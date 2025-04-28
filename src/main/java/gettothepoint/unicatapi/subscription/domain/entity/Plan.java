package gettothepoint.unicatapi.subscription.domain.entity;

import gettothepoint.unicatapi.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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
    public Plan(String name, String description, Long price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }

}
