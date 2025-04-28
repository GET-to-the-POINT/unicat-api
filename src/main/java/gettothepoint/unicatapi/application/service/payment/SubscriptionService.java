//package gettothepoint.unicatapi.application.service.payment;
//
//import gettothepoint.unicatapi.domain.entity.member.Member;
//import gettothepoint.unicatapi.domain.entity.payment.Plan;
//import gettothepoint.unicatapi.domain.entity.payment.Subscription;
//import gettothepoint.unicatapi.domain.repository.PlanRepository;
//import gettothepoint.unicatapi.subscription.domain.repository.SubscriptionRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class SubscriptionService {
//
//    private final SubscriptionRepository subscriptionRepository;
//    private final PlanRepository planRepository;
//
//    public void subscribe(Member member, Plan plan) {
//        Subscription subscription = member.getSubscription();
//        subscription.changePlan(plan);
//        subscriptionRepository.save(subscription);
//    }
//
//
//    public void createSubscription(Member member) {
//        Plan basicPlan = planRepository.findByName("BASIC")
//                .orElseThrow(() -> new IllegalStateException("기본 플랜이 존재하지 않습니다."));
//
//        Subscription subscription = Subscription.builder()
//                .member(member)
//                .plan(basicPlan)
//                .build();
//
//        member.setSubscription(subscription);
//    }
//
//    /**
//     * 반드시 자동 결제 해지와 연동 되어서 실행되어야한다.
//     * @param member 구독 해지할 회원
//     */
//
//    public void changeToBasicPlan(Member member) {
//        Plan basicPlan = planRepository.findByName("BASIC")
//                .orElseThrow(() -> new IllegalStateException("기본 플랜이 존재하지 않습니다."));
//
//        Subscription subscription = member.getSubscription();
//        subscription.changePlan(basicPlan);
//        subscriptionRepository.save(subscription);
//    }
//}