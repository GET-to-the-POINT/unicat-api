package gettothepoint.unicatapi.subscription.service;

import gettothepoint.unicatapi.subscription.entity.Plan;
import gettothepoint.unicatapi.subscription.persistence.PlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    PlanRepository planRepository;

    @InjectMocks
    PlanService planService;

    @Test
    void shouldReturnBasicPlanIfExists() {
        Plan mockPlan = mock(Plan.class);
        when(planRepository.findByName("BASIC")).thenReturn(Optional.of(mockPlan));

        Plan result = planService.getBasicPlan();

        assertThat(result).isSameAs(mockPlan);
    }

    @Test
    void shouldThrowIfBasicPlanNotFound() {
        when(planRepository.findByName("BASIC")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> planService.getBasicPlan()).isInstanceOf(IllegalStateException.class).hasMessageContaining("기본 플랜이 존재하지 않습니다.");
    }
}