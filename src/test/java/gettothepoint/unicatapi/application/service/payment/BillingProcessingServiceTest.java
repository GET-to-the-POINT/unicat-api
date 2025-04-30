package gettothepoint.unicatapi.application.service.payment;

import gettothepoint.unicatapi.member.domain.Member;
import gettothepoint.unicatapi.payment.domain.Billing;
import gettothepoint.unicatapi.payment.domain.Order;
import gettothepoint.unicatapi.payment.persistence.BillingRepository;
import gettothepoint.unicatapi.payment.application.BillingProcessingService;
import gettothepoint.unicatapi.payment.application.OrderService;
import gettothepoint.unicatapi.payment.application.PaymentService;
import gettothepoint.unicatapi.subscription.domain.Plan;
import gettothepoint.unicatapi.subscription.domain.Subscription;
import gettothepoint.unicatapi.subscription.application.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void testExpiredSubscriptions() {
        // given
        Plan premiumPlan = Plan.builder().name("PREMIUM").price(10000L).build();
        Plan basicPlan = Plan.builder().name("BASIC").price(0L).build();

        Member member = new Member();
        Subscription subscription = new Subscription(member, premiumPlan);
        member.setSubscription(subscription);

        Billing expiredBilling = mock(Billing.class);
        when(expiredBilling.getMember()).thenReturn(member);

        LocalDate expiredDate = LocalDate.now().minusDays(1);
        LocalDateTime startOfDay = expiredDate.atStartOfDay();
        LocalDateTime endOfDay = expiredDate.atTime(LocalTime.MAX);

        when(billingRepository.findNonRecurringMembersWithExpiredSubscription(startOfDay, endOfDay))
                .thenReturn(List.of(expiredBilling));

        // 실제 서비스 메서드 동작을 모킹
        doAnswer(invocation -> {
            Member m = invocation.getArgument(0);
            Subscription s = m.getSubscription();
            s.changePlan(basicPlan); // 여기서 직접 basicPlan으로 변경
            return null;
        }).when(subscriptionService).expiredThenChangeBasicPlan(any(Member.class));

        // when
        billingProcessingService.processExpiredSubscriptions();

        // then
        assertEquals("BASIC", member.getSubscription().getPlan().getName());
        verify(subscriptionService).expiredThenChangeBasicPlan(any(Member.class));
    }

    @Test
    void testRecurringPayments() {
        // given
        Member member = new Member();
        Plan premiumPlan = Plan.builder().name("PREMIUM").price(10000L).build();
        Subscription subscription = new Subscription(member, premiumPlan);
        member.setSubscription(subscription);

        Billing recurringBilling = mock(Billing.class);
        when(recurringBilling.getMember()).thenReturn(member);

        Order createdOrder = mock(Order.class);
        when(orderService.create(member, premiumPlan)).thenReturn(createdOrder);

        Map<String, Object> paymentResult = Map.of(
                "status", "success",
                "paymentKey", "test-payment-key",
                "orderId", "test-order-id"
        );
        when(paymentService.approveAutoPayment(createdOrder, recurringBilling)).thenReturn(paymentResult);

        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        when(billingRepository.findAllByLastPaymentDateBeforeAndRecurring(oneMonthAgo, Boolean.TRUE))
                .thenReturn(List.of(recurringBilling));

        // when
        billingProcessingService.processRecurringPayments();

        // then
        verify(orderService).create(member, premiumPlan);
        verify(paymentService).approveAutoPayment(createdOrder, recurringBilling);
    }
}