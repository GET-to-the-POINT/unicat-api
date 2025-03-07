package gettothepoint.unicatapi.application.service.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import gettothepoint.unicatapi.domain.entity.Member;
import gettothepoint.unicatapi.domain.entity.Order;
import gettothepoint.unicatapi.domain.entity.Subscription;
import gettothepoint.unicatapi.domain.repository.SubscriptionRepository;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public void createSubscription(Member member, Order order) {
        Subscription subscription = Subscription.builder()
                .member(member)
                .order(order)
                .build();
        subscriptionRepository.save(subscription);
    }
}
