package gettothepoint.unicatapi.application.service.payment;

import gettothepoint.unicatapi.member.application.MemberService;
import gettothepoint.unicatapi.member.domain.Member;
import gettothepoint.unicatapi.domain.entity.payment.Billing;
import gettothepoint.unicatapi.domain.entity.payment.Order;
import gettothepoint.unicatapi.infrastructure.gateway.TossPaymentGateway;
import gettothepoint.unicatapi.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TossPaymentGateway tossPaymentGateway;
    private final OrderService orderService;
    private final BillingService billingService;
    private final SubscriptionService subscriptionService;
    private final PaymentRecordService paymentRecordService;
    private final MemberService memberService;

    @Transactional
    public Map<String, Object> approveAutoPayment(Long memberId) {
        Member member = memberService.getOrElseThrow(memberId);
        Billing billing = member.getBilling();
        Order order = member.getOrders().stream()
                .filter(Order::isPending)
                .sorted()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "진행 중인 주문이 없습니다."));

        return approveAutoPayment(order, billing);
    }

    public Map<String, Object> approveAutoPayment(Order order, Billing billing) {
        String email = billing.getMember().getEmail();

        Map<String, Object> approvalResult = tossPaymentGateway.requestApproval(
                order, billing.getBillingKey(), email
        );
        orderService.markAsDone(order);
        paymentRecordService.save(order, approvalResult);
        billingService.applyRecurring(billing); //recurring 갱신
        subscriptionService.changePlan(order.getMember(), order.getPlan());

        return approvalResult;
    }
}