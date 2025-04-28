package gettothepoint.unicatapi.application.service.subscription;

import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.subscription.domain.entity.Plan;
import gettothepoint.unicatapi.subscription.domain.entity.Subscription;
import gettothepoint.unicatapi.domain.repository.PlanRepository;
import gettothepoint.unicatapi.subscription.domain.repository.SubscriptionRepository;
import gettothepoint.unicatapi.subscription.application.SubscriptionUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

class SubscriptionUseCaseTest {

    private SubscriptionRepository subscriptionRepository;
    private SubscriptionUseCase subscriptionUseCase;

    @BeforeEach
    void setUp() {
        PlanRepository planRepository = mock(PlanRepository.class);
        subscriptionRepository = mock(SubscriptionRepository.class);
        subscriptionUseCase = new SubscriptionUseCase(subscriptionRepository, planRepository);
    }

    @Test
    void testChangePlan() {
        // given
        Plan originalPlan = Plan.builder().name("BASIC").price(0L).build();
        Plan newPlan = Plan.builder().name("PREMIUM").price(10000L).build();

        Subscription subscription = Subscription.builder()
                .plan(originalPlan)
                .build();

        Member member = Member.builder().build();
        member.setSubscription(subscription); // 연관관계 설정

        // when
        subscriptionUseCase.changePlan(member, newPlan);

        // then
        assertNotNull(member.getSubscription()); // 구독이 존재하는지
        assertEquals("PREMIUM", member.getSubscription().getPlan().getName()); // 이름이 바뀌었는지
        assertEquals(10000L, member.getSubscription().getPlan().getPrice().longValue());//가격도 바뀌었는지
        verify(subscriptionRepository, times(1)).save(subscription);//저장이 되었는지
    }
}