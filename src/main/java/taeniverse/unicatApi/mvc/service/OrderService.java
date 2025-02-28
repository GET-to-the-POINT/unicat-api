package taeniverse.unicatApi.mvc.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import taeniverse.unicatApi.mvc.model.dto.OrderRequest;
import taeniverse.unicatApi.mvc.model.entity.Member;
import taeniverse.unicatApi.mvc.model.entity.Order;
import taeniverse.unicatApi.mvc.model.entity.Subscription;
import taeniverse.unicatApi.mvc.repository.MemberRepository;
import taeniverse.unicatApi.mvc.repository.OrderRepository;
import taeniverse.unicatApi.mvc.repository.SubscriptionRepository;
import taeniverse.unicatApi.payment.PayType;
import taeniverse.unicatApi.payment.TossPaymentStatus;



@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public String createOrUpdateOrder(OrderRequest orderRequest, Long memberId) {
        // 회원 조회 (없으면 예외 발생)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원이 존재하지 않습니다: " + memberId));
        // 가주문 생성 시, 해당 사용자의 구독 정보가 없다면 임시 구독 생성 (구독 상태를 "pending"으로 설정)
        Subscription subscription = subscriptionRepository.findByMember(member)
                .orElseGet(() -> {
                    Subscription newSubscription = new Subscription();
                    newSubscription.setMember(member);
                    newSubscription.setStatus("pending"); // 임시 구독 상태 (결제 완료 후 "active"로 업데이트)
                    return subscriptionRepository.save(newSubscription);
                });

        // 가주문 생성 (구독 정보를 함께 연결)
        Order order = Order.builder()
                .orderName(orderRequest.getOrderName())
                .amount(orderRequest.getAmount())
                .payMethod(orderRequest.getPayMethod())
                .status(TossPaymentStatus.PENDING)
                .member(member)
                .subscription(subscription) // 생성한 구독 정보 할당
                .build();

        orderRepository.save(order);
        log.info("Order created: orderId={}, userId={}", order.getOrderId(), member.getId());
        return order.getOrderId();
    }

    @Transactional(readOnly = true)
    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
    }

    @Retryable(
            value = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 200)
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order finalizeOrder(String orderId, TossPaymentStatus newStatus, PayType payMethod) {
        // 영속 상태의 주문을 비관적 락으로 조회합니다.
        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        // 주문 객체의 필드를 수정합니다.
        order.setStatus(newStatus);
        order.setPayMethod(payMethod);

        // 구독 상태도 ACTIVE로 업데이트
        order.getSubscription().setStatus("active");

        log.info("Order finalized: {} -> {} with payMethod {}", order.getOrderId(), newStatus, payMethod);

        // 주문 엔티티가 영속 상태이면, save() 호출은 UPDATE 쿼리를 발생시킵니다.
        return orderRepository.save(order);
    }
}