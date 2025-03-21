package gettothepoint.unicatapi.application.service.payment;

import gettothepoint.unicatapi.domain.entity.payment.Billing;
import gettothepoint.unicatapi.domain.entity.payment.Order;
import gettothepoint.unicatapi.infrastructure.gateway.TossPaymentGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TossPaymentGateway tossPaymentGateway;
    private final OrderService orderService;
    private final BillingService billingService;
    private final SubscriptionService subscriptionService;

    public Map<String, Object> approveAutoPayment(String memberEmail) {
        Order order = orderService.getPendingOrderByEmail(memberEmail);
        return this.approve(memberEmail, order);
    }

    public Map<String, Object> approve(String memberEmail, Order order) {

        Billing billing = billingService.getBillingForMember(order.getMember());

        Map<String, Object> approvalResult = tossPaymentGateway.requestApproval(
                order, billing.getBillingKey(), memberEmail
        );

        orderService.markAsDone(order);
        billingService.applyRecurring(billing); //recurring 갱신
        subscriptionService.create(order.getMember(), order.getSubscriptionPlan());

        return approvalResult;
    }
}