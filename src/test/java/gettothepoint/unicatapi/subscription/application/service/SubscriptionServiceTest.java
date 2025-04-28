package gettothepoint.unicatapi.subscription.application.service;

import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.subscription.domain.entity.Plan;
import gettothepoint.unicatapi.subscription.domain.entity.Subscription;
import gettothepoint.unicatapi.subscription.domain.repository.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {
    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PlanService planService;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Test
    void shouldCreateNewSubscription()
    {
        // given
        Member member = new Member();
        Plan basicPlan = Plan.builder()
                .name("BASIC")
                .price(0L)
                .build();
        when(planService.getBasicPlan()).thenReturn(basicPlan);

        // when
        subscriptionService.createSubscription(member);

        // then
        assertThat(member.getSubscription())
                .isNotNull()
                .extracting(s -> s.getPlan().getName())
                .isEqualTo("BASIC");
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void shouldChangePlan()
    {
        // given
        Member member = new Member();
        Plan basicPlan = Plan.builder().name("BASIC").build();
        Subscription subscription = new Subscription(member, basicPlan);
        Plan premiumPlan = Plan.builder().name("PREMIUM").build();

        // when
        subscriptionService.changePlan(subscription, premiumPlan);

        // then
        assertThat(subscription)
                .extracting(s -> s.getPlan().getName())
                .isEqualTo("PREMIUM");
        verify(subscriptionRepository).save(subscription);
    }

    @Test
    void shouldChangeToBasicPlanWhenExpired()
    {
        // given
        Member member = new Member();
        Plan premiumPlan = Plan.builder().name("PREMIUM").build();
        Subscription subscription = new Subscription(member, premiumPlan);
        member.setSubscription(subscription);

        Plan basicPlan = Plan.builder().name("BASIC").build();
        when(planService.getBasicPlan()).thenReturn(basicPlan);

        // subscription이 만료되었다고 가정
        ReflectionTestUtils.setField(subscription.getPeriod(), "endDate", LocalDateTime.now().minusDays(1));

        // when
        subscriptionService.checkAndExpireIfNeeded(member);

        // then
        assertThat(member.getSubscription().getPlan().getName()).isEqualTo("BASIC");
        verify(subscriptionRepository).save(any(Subscription.class));
    }
}