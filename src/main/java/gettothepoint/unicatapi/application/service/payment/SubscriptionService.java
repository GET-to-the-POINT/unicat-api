package gettothepoint.unicatapi.application.service.payment;

import gettothepoint.unicatapi.domain.constant.payment.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.payment.Order;
import gettothepoint.unicatapi.domain.entity.payment.Subscription;
import gettothepoint.unicatapi.domain.repository.SubscriptionRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

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

    @Transactional
    public void cancelSubscriptionByOrder(Order order) {
        Optional<Subscription> subscriptionOpt = subscriptionRepository.findByOrder(order);
        Subscription subscription = subscriptionOpt
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found"));
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(subscription);
    }
}
