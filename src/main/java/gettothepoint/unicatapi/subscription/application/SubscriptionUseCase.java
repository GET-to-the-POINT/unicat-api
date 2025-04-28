package gettothepoint.unicatapi.subscription.application;

import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.subscription.application.service.PlanService;
import gettothepoint.unicatapi.subscription.application.service.SubscriptionService;
import gettothepoint.unicatapi.subscription.domain.entity.Plan;
import gettothepoint.unicatapi.subscription.domain.entity.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 구독 관련 비즈니스 로직을 처리하는 유스케이스
 * 구독 생성, 변경, 만료 처리 등을 담당합니다.
 */
@Service
@RequiredArgsConstructor
public class SubscriptionUseCase {

    private final SubscriptionService subscriptionService;
    private final PlanService planService;

    /**
     * 멤버에게 구독을 새로 생성합니다.
     * 회원 가입 직후 등에 호출됩니다.
     * 
     * @param member 구독을 생성할 멤버
     */
    @Transactional
    public void createSubscription(Member member) {
        subscriptionService.createSubscription(member);
    }

    /**
     * 현재 구독을 BASIC 플랜으로 전환합니다.
     * 구독 만료 등 자동 전환 시 사용됩니다.
     * 
     * @param member 플랜을 변경할 멤버
     */
    @Transactional
    public void changeToBasicPlan(Member member) {
        subscriptionService.changeToBasicPlan(member);
    }

    /**
     * 원하는 이름의 플랜으로 구독을 변경합니다.
     * 
     * @param member 구독을 변경할 멤버
     * @param planName 변경할 플랜 이름
     */
    @Transactional
    public void changePlan(Member member, String planName) {
        Subscription subscription = member.getSubscription();
        Plan plan = planService.getPlanByName(planName);
        subscriptionService.changePlan(subscription, plan);
    }

    /**
     * 구독이 만료되었는지 확인하고, 만료된 경우 기본 플랜으로 변경합니다.
     * 스케줄러나 마이페이지 확인 시 호출됩니다.
     * 
     * @param member 구독 상태를 확인할 멤버
     */
    @Transactional
    public void checkAndExpireIfNeeded(Member member) {
        subscriptionService.checkAndExpireIfNeeded(member);
    }
}
