package gettothepoint.unicatapi.application.service;

import gettothepoint.unicatapi.application.service.payment.OrderService;
import gettothepoint.unicatapi.application.service.payment.PaymentService;
import gettothepoint.unicatapi.domain.entity.payment.Billing;
import gettothepoint.unicatapi.domain.entity.payment.Order;
import gettothepoint.unicatapi.domain.repository.BillingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingScheduler {

    private final BillingRepository billingRepository;
    private final PaymentService paymentService;
    private final OrderService orderService;

    @Scheduled(cron = "0 0 3 * * ?") // 매일 새벽 3시 실행
    @Transactional
    public void processAutoBilling() {
        log.info("🚀 자동 결제 스케줄링 실행 중...");

        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        List<Billing> billingList = billingRepository
                .findAllByLastPaymentDateBeforeAndRecurring(oneMonthAgo, Boolean.TRUE);

        if (billingList.isEmpty()) {
            log.info("⏳ 자동 결제 대상이 없습니다.");
            return;
        }

        for (Billing billing : billingList) {
            String email = billing.getMember().getEmail();
            try {
                Order order = orderService.create(billing.getMember().getId(), billing.getSubscriptionPlan());
                paymentService.approveAutoPayment(email);

                log.info("{}님 {} 자동 결제 성공 ({}원)", email, billing.getSubscriptionPlan().getKoreanName(), billing.getSubscriptionPlan().getPrice());
            } catch (Exception e) {
                log.error("{}님 자동 결제 실패: {}", email, e.getMessage());
            }
        }
    }
}