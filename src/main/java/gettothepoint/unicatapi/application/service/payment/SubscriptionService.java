package gettothepoint.unicatapi.application.service.payment;

import gettothepoint.unicatapi.domain.constant.payment.SubscriptionPlan;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.payment.Subscription;
import gettothepoint.unicatapi.domain.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public void create(Member member, SubscriptionPlan plan) {

        if (member.getSubscriptions() != null) {
            Subscription current = member.getSubscriptions();
            current.setInactive(); // active -> false
            subscriptionRepository.save(current);
        }

        Subscription subscription = Subscription.builder()
                .member(member)
                .subscriptionPlan(plan)
                .build();

        subscriptionRepository.save(subscription);
    }
}