package gettothepoint.unicatapi.application.service.payment;

import gettothepoint.unicatapi.application.service.member.MemberService;
import gettothepoint.unicatapi.domain.constant.payment.TossPaymentStatus;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.payment.Order;
import gettothepoint.unicatapi.domain.entity.payment.Plan;
import gettothepoint.unicatapi.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberService memberService;

    public Order create(Long memberId, Plan plan) {
        Member member = memberService.getOrElseThrow(memberId);

        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .orderName(plan.getName())
                .amount(plan.getPrice())
                .plan(plan)
                .member(member)
                .status(TossPaymentStatus.PENDING)
                .build();

        return orderRepository.save(order);
    }

    public void markAsDone(Order order) {
        order.markDone();
        orderRepository.save(order);
    }

}
