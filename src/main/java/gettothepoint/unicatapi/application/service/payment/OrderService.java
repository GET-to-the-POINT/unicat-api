package gettothepoint.unicatapi.application.service.payment;

import gettothepoint.unicatapi.member.application.MemberService;
import gettothepoint.unicatapi.member.domain.Member;
import gettothepoint.unicatapi.domain.entity.payment.Order;
import gettothepoint.unicatapi.domain.repository.OrderRepository;
import gettothepoint.unicatapi.subscription.entity.Plan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberService memberService;

    public Order create(Long memberId, Plan plan) {
        Member member = memberService.getOrElseThrow(memberId);

        Order order = Order.builder()
                .orderName(plan.getName())
                .amount(plan.getPrice())
                .plan(plan)
                .member(member)
                .status("PENDING")
                .build();

        return orderRepository.save(order);
    }

    public void markAsDone(Order order) {
        order.markDone();
        orderRepository.save(order);
    }

}
