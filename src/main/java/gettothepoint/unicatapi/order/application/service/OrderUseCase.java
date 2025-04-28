package gettothepoint.unicatapi.order.application.service;

import gettothepoint.unicatapi.application.service.member.MemberService;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.order.domain.entity.Order;
import gettothepoint.unicatapi.subscription.application.service.PlanService;
import gettothepoint.unicatapi.subscription.domain.entity.Plan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@RequiredArgsConstructor
public class OrderUseCase {

    private final MemberService memberService;
    private final OrderService orderService;
    private final PlanService planService;

    public Order create(Long memberId, Long planId) {
        Member member = memberService.getOrElseThrow(memberId);
        Plan plan = planService.getOrElseThrow(planId);
        return orderService.create(member, plan);
    }

    public void markAsDone(String orderId) {
        Order order = orderService.getOrElseThrow(orderId);
        orderService.markAsDone(order);
    }
}
