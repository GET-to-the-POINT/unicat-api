package gettothepoint.unicatapi.application.service;

import gettothepoint.unicatapi.application.service.payment.PaymentService;
import gettothepoint.unicatapi.domain.dto.payment.OrderRequest;
import gettothepoint.unicatapi.domain.dto.payment.PaymentApprovalRequest;
import gettothepoint.unicatapi.domain.entity.payment.Billing;
import gettothepoint.unicatapi.domain.repository.BillingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingScheduler {

    private final BillingRepository billingRepository;
    private final PaymentService paymentService;

    @Scheduled(cron = "0 0 3 * * ?") // 매일 새벽 3시 실행
    @Transactional
    public void processAutoBilling() {
        log.info("🚀 자동 결제 스케줄링 실행 중...");

        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        List<Billing> billingList = billingRepository.findAllByLastPaymentDateBefore(oneMonthAgo);

        if (billingList.isEmpty()) {
            log.info("⏳ 자동 결제 대상이 없습니다.");
            return;
        }

        for (Billing billing : billingList) {
            try {
                // 1. 주문 정보 생성 (구독 결제)
                OrderRequest orderRequest = new OrderRequest("구독 결제", billing.getAmount());

                // 2. 주문 ID를 임의로 생성 (실제 환경에서는 주문 생성 로직에 따라 ID를 발급받아야 합니다)
                String generatedOrderId = UUID.randomUUID().toString();

                // 3. PaymentApprovalRequest 생성 (주문 정보와 회원 정보를 사용)
                PaymentApprovalRequest approvalRequest = new PaymentApprovalRequest();
                approvalRequest.setAmount(orderRequest.getAmount());
                approvalRequest.setCustomerKey(billing.getMember().getCustomerKey());
                approvalRequest.setOrderId(generatedOrderId);
                approvalRequest.setOrderName(orderRequest.getOrderName());

                // 4. 자동 결제 승인 요청
                paymentService.approveAutoPayment(billing.getBillingKey(), approvalRequest);

                // 5. 결제 성공 시 빌링키의 마지막 결제일 업데이트
                billing.updateLastPaymentDate(LocalDateTime.now());
                log.info("✅ {}님 자동 결제 성공 (금액: {})", billing.getMember().getEmail(), billing.getAmount());
            } catch (Exception e) {
                log.error("❌ {}님 자동 결제 실패: {}", billing.getMember().getEmail(), e.getMessage());
            }
        }
    }
}
