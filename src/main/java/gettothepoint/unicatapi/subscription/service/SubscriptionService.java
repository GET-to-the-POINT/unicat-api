package gettothepoint.unicatapi.subscription.service;

import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.subscription.entity.Plan;
import gettothepoint.unicatapi.subscription.entity.Subscription;
import gettothepoint.unicatapi.subscription.persistence.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 구독 관련 도메인 로직을 처리하는 서비스
 * 리포지토리 접근 및 실제 구현을 담당합니다.
 */
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanService planService;

    /**
     * 멤버에게 구독을 새로 생성
     * (회원 가입 직후 등)
     */
    @Transactional
    public void createSubscription(Member member) {
        Plan basicPlan = planService.getBasicPlan();

        Subscription subscription = Subscription.builder()
                .member(member)
                .plan(basicPlan)
                .build();

        member.setSubscription(subscription); // 양방향 연관 설정
        subscriptionRepository.save(subscription);
    }

    public void changePlan(Member member, Plan plan) {
        Subscription subscription = member.getSubscription();
        subscription.changePlan(plan);
        subscriptionRepository.save(subscription);
    }

    /**
     * 만료되었으면 기본 플랜으로 변경 스케줄러나 마이페이지 확인용
     */
    @Transactional
    public void expiredThenChangeBasicPlan(Member member) {
        Subscription subscription = member.getSubscription();
        if (!subscription.isExpired()) {
            return;
        }

        Plan basicPlan = planService.getBasicPlan();
        subscription.changePlan(basicPlan);
    }
}
