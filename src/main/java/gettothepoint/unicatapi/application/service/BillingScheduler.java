package gettothepoint.unicatapi.application.service;

import gettothepoint.unicatapi.application.service.payment.OrderService;
import gettothepoint.unicatapi.application.service.payment.PaymentService;
import gettothepoint.unicatapi.application.service.payment.SubscriptionService;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.payment.Billing;
import gettothepoint.unicatapi.domain.entity.payment.Order;
import gettothepoint.unicatapi.domain.repository.BillingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingScheduler {

    private final BillingRepository billingRepository;
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final SubscriptionService subscriptionService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void processAutoBilling() {
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        List<Billing> recurringList = billingRepository
                .findAllByLastPaymentDateBeforeAndRecurring(oneMonthAgo, Boolean.TRUE);
        List<Billing> unrecurringList = List.of(); // TODO : 구독 만료 처리 베이직 플랜 변경

        // 이미 유료 구독자들만 대상으로 진행된다.
        for (Billing billing : recurringList) {
            Member member = billing.getMember();
            Order order = orderService.create(member.getEmail(), member.getSubscription().getSubscriptionPlan());
            paymentService.approveAutoPayment(order, billing);
        }
        
        // TODO: 똑바로 만들기
        for (Billing billing : unrecurringList) {
            Member member = billing.getMember();
            subscriptionService.changeBasePlan(member);
        }
    }
}