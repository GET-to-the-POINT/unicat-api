package gettothepoint.unicatapi.payment.application;

import gettothepoint.unicatapi.member.domain.Member;
import gettothepoint.unicatapi.payment.domain.Order;
import gettothepoint.unicatapi.payment.persistence.OrderRepository;

import gettothepoint.unicatapi.subscription.domain.Plan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public Order create(Member member, Plan plan) {
        Order order = Order.createOrder(plan.getName(), plan.getPrice(), member, plan);
        return orderRepository.save(order);
    }

    public void markAsDone(Order order) {
        order.markDone();
        orderRepository.save(order);
    }

    public Order getOrElseThrow(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("주문이 존재하지 않습니다: " + orderId));
    }
}
