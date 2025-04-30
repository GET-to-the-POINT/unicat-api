package gettothepoint.unicatapi.payment.presentation;

import gettothepoint.unicatapi.common.gateway.TossPaymentGateway;
import gettothepoint.unicatapi.member.application.MemberService;
import gettothepoint.unicatapi.member.domain.Member;
import gettothepoint.unicatapi.payment.application.BillingService;
import gettothepoint.unicatapi.payment.application.OrderService;
import gettothepoint.unicatapi.payment.application.PaymentRecordService;
import gettothepoint.unicatapi.payment.application.PaymentService;
import gettothepoint.unicatapi.payment.domain.Billing;
import gettothepoint.unicatapi.payment.domain.Order;
import gettothepoint.unicatapi.payment.persistence.BillingRepository;
import gettothepoint.unicatapi.payment.persistence.PaymentRepository;
import gettothepoint.unicatapi.subscription.application.SubscriptionService;
import gettothepoint.unicatapi.subscription.domain.Plan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BillingRepository billingRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private OrderService orderService;

    @Mock
    private BillingService billingService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private TossPaymentGateway tossPaymentGateway;

    @Mock
    private PaymentRecordService paymentRecordService;

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private Member member;

    @Mock
    private Billing billing;

    @BeforeEach
    void setUp() {
        // 공통 설정이 필요하면 여기에 작성
    }

    @Test
    void orderShouldBeCompletedWhenPaymentIsSuccessful() {
        // given
        UUID memberId = UUID.randomUUID();

        // 주문 생성 및 설정
        Order order = mock(Order.class);
        when(order.isPending()).thenReturn(true); // 주문이 진행 중임을 설정
        when(order.getMember()).thenReturn(member); // 주문에서 회원 반환하도록 설정
        when(order.getPlan()).thenReturn(mock(Plan.class)); // 주문에서 플랜 반환하도록 설정

        // 회원 설정
        List<Order> orders = new ArrayList<>();
        orders.add(order);

        when(member.getOrders()).thenReturn(orders); // 회원에게 주문 목록 추가
        when(member.getEmail()).thenReturn("test@example.com"); // 이메일 설정
        when(member.getBilling()).thenReturn(billing); // 회원의 빌링 설정

        when(memberService.getOrElseThrow(memberId)).thenReturn(member);

        // Billing 설정
        when(billing.getMember()).thenReturn(member); // billing에서 member를 반환하도록 설정
        when(billing.getBillingKey()).thenReturn("test-billing-key"); // billingKey 설정

        // TossPaymentGateway 모킹 및 설정
        Map<String, Object> approvalResult = new HashMap<>();
        approvalResult.put("status", "success");
        approvalResult.put("paymentKey", "test-payment-key");
        approvalResult.put("orderId", "test-order-id");

        // tossPaymentGateway에서 requestApproval 메서드가 호출될 때 결과를 반환하도록 설정
        when(tossPaymentGateway.requestApproval(
                any(Order.class),
                anyString(),
                anyString()
        )).thenReturn(approvalResult);

        // when
        Map<String, Object> result = paymentService.approveAutoPayment(memberId);

        // then
        verify(orderService).markAsDone(order); // 주문이 완료 상태로 변경되었는지 확인
        verify(billingService).applyRecurring(billing);  // 정기 결제가 갱신되었는지 확인
        verify(subscriptionService).changePlan(member, order.getPlan()); // 구독 플랜이 변경되었는지 확인
        verify(paymentRecordService).save(eq(order), any()); // 결제 기록이 저장되었는지 확인

        assertNotNull(result);
        assertEquals("success", result.get("status"));
    }
}