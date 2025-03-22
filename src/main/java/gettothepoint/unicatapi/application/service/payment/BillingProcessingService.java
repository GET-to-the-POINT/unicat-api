package gettothepoint.unicatapi.application.service.payment;

import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.payment.Billing;
import gettothepoint.unicatapi.domain.entity.payment.Order;
import gettothepoint.unicatapi.domain.repository.BillingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingProcessingService {

    private final BillingRepository billingRepository;
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final SubscriptionService subscriptionService;

    /**
     * 이미 결제가 진행중인(유료) 회원에 대해 자동 결제 처리
     * (마지막 결제일이 한 달 전보다 이전인 회원 대상)
     */
    @Transactional
    public void processRecurringPayments() {
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        List<Billing> recurringList = billingRepository.findAllByLastPaymentDateBeforeAndRecurring(oneMonthAgo, Boolean.TRUE);

        log.info("자동 결제 대상 Billing 수: {}", recurringList.size());
        for (Billing billing : recurringList) {
            Member member = billing.getMember();
            Order order = orderService.create(member.getEmail(), member.getSubscription().getSubscriptionPlan());
            paymentService.approveAutoPayment(order, billing);
            log.info("자동 결제 처리 완료: 회원 {}", member.getEmail());
        }
    }

    /**
     * 구독이 만료된 회원(구독 만료일이 어제와 같은 경우)에 대해 기본(BASIC) 플랜으로 전환 처리
     */
    @Transactional
    public void processExpiredSubscriptions() {
        // 만료일 조건: 구독이 어제까지 사용된 경우 → 어제 날짜를 기준으로 검색
        LocalDate expiredDate = LocalDate.now().minusDays(1);
        List<Billing> expiredList = billingRepository.findNonRecurringMembersWithExpiredSubscription(expiredDate);

        log.info("구독 만료 대상 Billing 수: {}", expiredList.size());
        for (Billing billing : expiredList) {
            Member member = billing.getMember();
            subscriptionService.changeBasePlan(member);
            log.info("구독 BASIC 전환 완료: 회원 {}", member.getEmail());
        }
    }
}
