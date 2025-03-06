package getToThePoint.unicatApi.application.service.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import getToThePoint.unicatApi.domain.entity.Member;
import getToThePoint.unicatApi.domain.entity.Order;
import getToThePoint.unicatApi.domain.entity.Subscription;
import getToThePoint.unicatApi.mvc.repository.SubscriptionRepository;

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
