package gettothepoint.unicatapi.subscription.application.service;

import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.subscription.domain.entity.Plan;
import gettothepoint.unicatapi.subscription.domain.entity.Subscription;
import gettothepoint.unicatapi.subscription.domain.repository.SubscriptionRepository;
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

    /**
     * 현재 구독을 BASIC 플랜으로 전환
     * (구독 만료 등 자동 전환 시 사용)
     */
    @Transactional
    public void changeToBasicPlan(Member member) {
        Plan basicPlan = planService.getBasicPlan();
        Subscription subscription = member.getSubscription();

        subscription.expireToBasicPlan(basicPlan);
        subscriptionRepository.save(subscription);
    }

    /**
     * 특정 플랜으로 구독 변경
     */
    @Transactional
    public void changePlan(Subscription subscription, Plan plan) {
        subscription.changePlan(plan);
        subscriptionRepository.save(subscription);
    }

    /**
     * 만료되었으면 기본 플랜으로 변경 스케줄러나 마이페이지 확인용
     */
    @Transactional
    public void checkAndExpireIfNeeded(Member member) {
        Subscription subscription = member.getSubscription();
        if (subscription.isExpired()) {
            changeToBasicPlan(member);
        }
    }
}
