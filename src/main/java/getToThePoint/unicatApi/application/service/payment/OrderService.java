package getToThePoint.unicatApi.application.service.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import getToThePoint.unicatApi.domain.dto.payment.OrderRequest;
import getToThePoint.unicatApi.domain.entity.Member;
import getToThePoint.unicatApi.domain.entity.Order;
import getToThePoint.unicatApi.mvc.repository.OrderRepository;
import getToThePoint.unicatApi.domain.constant.payment.TossPaymentStatus;


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
        Order order = findOrderById(orderId);
        updateOrderStatus(order, status);
    }

    private Order findOrderById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private void updateOrderStatus(Order order, TossPaymentStatus status) {
        order.setStatus(status);
        orderRepository.save(order);
    }
}
