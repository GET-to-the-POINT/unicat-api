package gettothepoint.unicatapi.application.service;

import gettothepoint.unicatapi.application.service.payment.OrderService;
import gettothepoint.unicatapi.application.service.payment.PaymentService;
import gettothepoint.unicatapi.domain.constant.payment.SubscriptionStatus;
import gettothepoint.unicatapi.domain.dto.payment.OrderRequest;
import gettothepoint.unicatapi.domain.dto.payment.PaymentApprovalRequest;
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

    @Scheduled(cron = "0 */3 * * * ?") // 매일 새벽 3시 실행
    @Transactional
    public void processAutoBilling() {
        log.info("🚀 자동 결제 스케줄링 실행 중...");

        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        List<Billing> billingList = billingRepository
                .findAllByLastPaymentDateBeforeAndSubscriptionStatus(oneMonthAgo, SubscriptionStatus.ACTIVE);

        if (billingList.isEmpty()) {
            log.info("⏳ 자동 결제 대상이 없습니다.");
            return;
        }
        for (Billing billing : billingList) {
            try {
                // 1. 주문 요청 정보를 생성합니다.
                OrderRequest orderRequest = new OrderRequest("구독 결제", billing.getAmount());

                // 2. createOrder 메서드를 통해 새 주문(Order)을 생성합니다.
                //    billing에 연결된 멤버의 id를 사용합니다.
                Order newOrder = orderService.createOrder(orderRequest, billing.getMember().getId());
                billing.setOrder(newOrder);

                // 3. PaymentApprovalRequest에 새 주문 번호를 할당합니다.
                PaymentApprovalRequest approvalRequest = new PaymentApprovalRequest();
                approvalRequest.setAmount(orderRequest.getAmount());
                approvalRequest.setCustomerKey(billing.getMember().getCustomerKey());
                approvalRequest.setOrderId(newOrder.getId());
                approvalRequest.setOrderName(orderRequest.getOrderName());

                // 4. 토스 결제 승인 요청 실행 (새 주문번호 사용)
                paymentService.approveAutoPayment(billing.getBillingKey(), approvalRequest);

                // 5. 결제 성공 시 Billing의 마지막 결제일 업데이트
                billing.updateLastPaymentDate(LocalDate.now());
                billingRepository.save(billing);

                log.info("✅ {}님 자동 결제 성공 (금액: {})", billing.getMember().getEmail(), billing.getAmount());
            } catch (Exception e) {
                log.error("❌ {}님 자동 결제 실패: {}", billing.getMember().getEmail(), e.getMessage());
            }
        }
    }
}