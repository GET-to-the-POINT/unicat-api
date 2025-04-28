package gettothepoint.unicatapi.subscription.application.service;

import gettothepoint.unicatapi.subscription.domain.entity.Plan;
import gettothepoint.unicatapi.subscription.domain.repository.PlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {
    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private PlanService planService;

    @Test
    void shouldFindPlanByName()
    {
        // given
        Plan premium = Plan.builder()
                .name("PREMIUM")
                .price(10000L)
                .build();
        when(planRepository.findByName("PREMIUM")).thenReturn(Optional.of(premium));

        // when
        Plan found = planService.getPlanByName("PREMIUM");

        // then
        assertThat(found)
                .extracting("name", "price")
                .containsExactly("PREMIUM", 10000L);
    }

    @Test
    void shouldThrowExceptionWhenPlanNotFound()
    {
        // given
        when(planRepository.findByName("NON_EXIST")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> planService.getPlanByName("NON_EXIST"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Plan not found");
    }

    @Test
    void shouldReturnBasicPlan()
    {
        // given
        Plan basic = Plan.builder()
                .name("BASIC")
                .price(0L)
                .build();
        when(planRepository.findByName("BASIC")).thenReturn(Optional.of(basic));

        // when
        Plan found = planService.getBasicPlan();

        // then
        assertThat(found)
                .extracting("name", "price")
                .containsExactly("BASIC", 0L);
    }
}