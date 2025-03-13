package gettothepoint.unicatapi.application.service.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import gettothepoint.unicatapi.domain.dto.payment.OrderRequest;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.payment.Order;
import gettothepoint.unicatapi.domain.repository.OrderRepository;
import gettothepoint.unicatapi.domain.constant.payment.TossPaymentStatus;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public Order create(OrderRequest orderRequest, Member member) {
        Order order = buildOrder(orderRequest, member);
        return orderRepository.save(order);
    }

    private Order buildOrder(OrderRequest orderRequest, Member member) {
        return Order.builder()
                .orderName(orderRequest.getOrderName())
                .amount(orderRequest.getAmount())
                .payMethod(orderRequest.getPayMethod())
                .status(TossPaymentStatus.PENDING)
                .member(member)
                .build();
    }

    public Order findById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    public void updateOrder(String orderId, TossPaymentStatus status) {
        Order order = findById(orderId);
        order.setStatus(status);
        orderRepository.save(order);
    }
}
