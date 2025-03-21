package gettothepoint.unicatapi.application.service.payment;

import gettothepoint.unicatapi.domain.constant.payment.SubscriptionPlan;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.payment.Subscription;
import gettothepoint.unicatapi.domain.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final BillingService billingService;

    public void subscribe(Member member, SubscriptionPlan plan) {
        Subscription subscription = member.getSubscription();
        subscription.changePlan(plan);
        subscriptionRepository.save(subscription);
    }

    /**
     * 반드시 자동 결제 해지와 연동 되어서 실행되어야한다.
     * @param member 구독 해지할 회원
     */
    public void unsubscribe(Member member) {
        billingService.cancelRecurringByMember(member.getId());
    }

    public void changeBasePlan(Member member) {
        member.getSubscription().changePlan(SubscriptionPlan.BASIC);
    }
}