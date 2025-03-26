package gettothepoint.unicatapi.infrastructure.config;

import gettothepoint.unicatapi.domain.entity.payment.Plan;
import gettothepoint.unicatapi.domain.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PlanRepository planRepository;

    @Override
    public void run(String... args) throws Exception {

        if (planRepository.count() == 0) {
            Plan basicPlan = Plan.builder()
                    .name("BASIC")
                    .description("기본 플랜")
                    .price(0L)
                    .build();

            Plan premiumPlan = Plan.builder()
                    .name("PREMIUM")
                    .description("프리미엄 플랜")
                    .price(2000L)
                    .build();

            planRepository.save(basicPlan);
            planRepository.save(premiumPlan);
        }
    }
}