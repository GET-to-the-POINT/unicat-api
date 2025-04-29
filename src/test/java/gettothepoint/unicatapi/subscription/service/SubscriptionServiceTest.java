package gettothepoint.unicatapi.subscription.service;

import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.subscription.entity.Plan;
import gettothepoint.unicatapi.subscription.entity.Subscription;
import gettothepoint.unicatapi.subscription.persistence.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    SubscriptionRepository subscriptionRepository;

    @Mock
    PlanService planService;

    @InjectMocks
    SubscriptionService subscriptionService;

    @Test
    void shouldCreateSubscriptionUsingBasicPlan() {
        Plan mockPlan = mock(Plan.class);
        Member mockMember = mock(Member.class);
        when(planService.getBasicPlan()).thenReturn(mockPlan);

        subscriptionService.createSubscription(mockMember);

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(captor.capture());
        verify(mockMember).setSubscription(captor.getValue());
        assertThat(captor.getValue().getPlan()).isSameAs(mockPlan);
    }

    @Test
    void shouldSwitchToBasicPlanIfExpired() {
        Plan mockPlan = mock(Plan.class);
        Member mockMember = mock(Member.class);
        Subscription mockSubscription = mock(Subscription.class);
        when(mockMember.getSubscription()).thenReturn(mockSubscription);
        when(mockSubscription.isExpired()).thenReturn(true);
        when(planService.getBasicPlan()).thenReturn(mockPlan);

        subscriptionService.expiredThenChangeBasicPlan(mockMember);

        verify(mockSubscription).changePlan(mockPlan);
    }
}