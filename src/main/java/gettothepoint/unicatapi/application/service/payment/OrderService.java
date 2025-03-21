package gettothepoint.unicatapi.application.service.payment;

import gettothepoint.unicatapi.domain.constant.payment.SubscriptionPlan;
import gettothepoint.unicatapi.domain.constant.payment.TossPaymentStatus;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.payment.Order;
import gettothepoint.unicatapi.domain.repository.MemberRepository;
import gettothepoint.unicatapi.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Order create(Long memberId, SubscriptionPlan plan) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .orderName(plan.getAutoOrderName())
                .amount(plan.getPrice())
                .subscriptionPlan(plan)
                .member(member)
                .status(TossPaymentStatus.PENDING)
                .build();

        return orderRepository.save(order);
    }

    @Transactional
    public void markAsDone(Order order) {
        order.markDone();
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getPendingOrderByEmail(String email) {
        return orderRepository.findFirstByMember_EmailAndStatusOrderByCreatedAtDesc(email, TossPaymentStatus.PENDING)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "진행 중인 주문이 없습니다."));
    }
}
