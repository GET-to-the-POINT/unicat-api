package taeniverse.unicatApi.mvc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import taeniverse.unicatApi.mvc.model.entity.Member;
import taeniverse.unicatApi.mvc.model.entity.Order;
import taeniverse.unicatApi.mvc.model.entity.Subscription;
import taeniverse.unicatApi.mvc.repository.SubscriptionRepository;

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
