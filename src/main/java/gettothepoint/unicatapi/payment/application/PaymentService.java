package gettothepoint.unicatapi.payment.application;

import gettothepoint.unicatapi.common.gateway.TossPaymentGateway;
import gettothepoint.unicatapi.member.application.MemberService;
import gettothepoint.unicatapi.member.domain.Member;
import gettothepoint.unicatapi.payment.domain.Billing;
import gettothepoint.unicatapi.payment.domain.Order;
import gettothepoint.unicatapi.subscription.application.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

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
    public Map<String, Object> approveAutoPayment(UUID memberId) {
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
        String customerKey = billing.getMember().getId().toString();

        Map<String, Object> approvalResult = tossPaymentGateway.requestApproval(
                order, billing.getBillingKey(), customerKey
        );
        orderService.markAsDone(order);
        paymentRecordService.save(order, approvalResult);
        billingService.applyRecurring(billing);
        subscriptionService.changePlan(order.getMember(), order.getPlan());

        return approvalResult;
    }
}