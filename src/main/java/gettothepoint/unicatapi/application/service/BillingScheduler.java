package gettothepoint.unicatapi.application.service;

import gettothepoint.unicatapi.application.service.payment.BillingProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingScheduler {

    private final BillingProcessingService billingProcessingService;

    // 매 3분마다 스케줄러 실행
    @Scheduled(cron = "0 */3 * * * ?")
    public void processAutoBilling() {
        log.info("자동 빌링 처리 시작");
        billingProcessingService.processRecurringPayments();
        billingProcessingService.processExpiredSubscriptions();
        log.info("자동 빌링 처리 완료");
    }
}