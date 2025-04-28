package gettothepoint.unicatapi.subscription.application;

import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.subscription.domain.entity.Plan;
import gettothepoint.unicatapi.subscription.domain.entity.Subscription;
import gettothepoint.unicatapi.domain.repository.PlanRepository;
import gettothepoint.unicatapi.subscription.domain.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;

    /**
     * 멤버에게 구독을 새로 생성
     * (회원 가입 직후 등)
     */
    public void createSubscription(Member member) {
        Plan basicPlan = getBasicPlan();

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
    public void changeToBasicPlan(Member member) {
        Plan basicPlan = getBasicPlan();
        Subscription subscription = member.getSubscription();

        subscription.expireToBasicPlan(basicPlan);
        subscriptionRepository.save(subscription);
    }

    /**
     * 원하는 이름의 플랜으로 구독 변경
     */
    public void changePlan(Member member, Plan plan) {
        Subscription subscription = member.getSubscription();
        subscription.changePlan(plan);
        subscriptionRepository.save(subscription);
    }

    /**
     * 기본(BASIC) 플랜 조회
     */
    private Plan getBasicPlan() {
        return planRepository.findByName("BASIC")
                .orElseThrow(() -> new IllegalStateException("기본 플랜이 존재하지 않습니다."));
    }
    /**
     * 만료되었으면 기본 플랜으로 변경 스케줄러나 마이페이지 확인용
     */
    public void checkAndExpireIfNeeded(Member member) {
        Subscription subscription = member.getSubscription();
        if (subscription.isExpired()) {
            changeToBasicPlan(member);
        }
    }
}