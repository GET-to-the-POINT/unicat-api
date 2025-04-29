package gettothepoint.unicatapi.application.service.payment;

import gettothepoint.unicatapi.member.domain.Member;
import gettothepoint.unicatapi.domain.entity.payment.Billing;
import gettothepoint.unicatapi.domain.repository.BillingRepository;
import gettothepoint.unicatapi.order.service.OrderService;
import gettothepoint.unicatapi.subscription.entity.Plan;
import gettothepoint.unicatapi.subscription.entity.Subscription;
import gettothepoint.unicatapi.subscription.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

class BillingProcessingServiceTest {

    private BillingRepository billingRepository;
    private PaymentService paymentService;
    private OrderService orderService;
    private SubscriptionService subscriptionService;
    private BillingProcessingService billingProcessingService;

    @BeforeEach
    void setUp() {
        billingRepository = mock(BillingRepository.class);
        paymentService = mock(PaymentService.class);
        orderService = mock(OrderService.class);
        subscriptionService = mock(SubscriptionService.class);

        billingProcessingService = new BillingProcessingService(
                billingRepository,
                paymentService,
                orderService,
                subscriptionService
        );
    }

    @Test
    void 구독_만료된_회원은_BASIC플랜으로_전환된다() {
        // given
        Plan premiumPlan = Plan.builder().name("PREMIUM").price(10000L).build();
        Plan basicPlan = Plan.builder().name("BASIC").price(0L).build();

        Member member = new Member();
        Subscription subscription = new Subscription(member, premiumPlan);
        member.setSubscription(subscription);

        Billing expiredBilling = mock(Billing.class);
        when(expiredBilling.getMember()).thenReturn(member);

        when(billingRepository.findNonRecurringMembersWithExpiredSubscription(any()))
                .thenReturn(List.of(expiredBilling));

        doAnswer(invocation -> {
            Member m = invocation.getArgument(0);
            Subscription s = m.getSubscription();
            s.changePlan(basicPlan); // 진짜 변경시켜주는 동작 흉내냄
            return null;
        }).when(subscriptionService).expiredThenChangeBasicPlan(any(Member.class));

        // when
        billingProcessingService.processExpiredSubscriptions();

        // then
        assertEquals("BASIC", member.getSubscription().getPlan().getName());
    }
}