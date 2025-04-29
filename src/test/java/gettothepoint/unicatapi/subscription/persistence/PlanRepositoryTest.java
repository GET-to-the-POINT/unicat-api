package gettothepoint.unicatapi.subscription.persistence;

import gettothepoint.unicatapi.subscription.domain.Plan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PlanRepositoryTest {

    @Autowired
    private PlanRepository planRepository;

    @Test
    @DisplayName("플랜 이름으로 조회 테스트")
    void findByName() {
        planRepository.save(Plan.builder()
                .name("BASIC")
                .price(1000L)
                .description("Basic Plan")
                .build());

        Optional<Plan> result = planRepository.findByName("BASIC");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("BASIC");
    }
}