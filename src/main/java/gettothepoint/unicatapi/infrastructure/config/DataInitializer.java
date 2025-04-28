package gettothepoint.unicatapi.infrastructure.config;

import gettothepoint.unicatapi.subscription.domain.entity.Plan;
import gettothepoint.unicatapi.subscription.domain.repository.PlanRepository;
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
                    .description("BASIC 플랜")
                    .price(0L)
                    .build();

            Plan premiumPlan = Plan.builder()
                    .name("PREMIUM")
                    .description("PREMIUM 플랜")
                    .price(10000L)
                    .build();

            Plan vipPlan = Plan.builder()
                    .name("VIP")
                    .description("VIP 플랜")
                    .price(20000L)
                    .build();

            planRepository.save(basicPlan);
            planRepository.save(premiumPlan);
            planRepository.save(vipPlan);
        }
    }
}