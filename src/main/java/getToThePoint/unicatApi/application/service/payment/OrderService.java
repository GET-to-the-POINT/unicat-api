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
        Order order = Order.builder()
                .orderName(orderRequest.getOrderName())
                .amount(orderRequest.getAmount())
                .payMethod(orderRequest.getPayMethod())
                .status(TossPaymentStatus.PENDING)
                .member(member)
                .build();

        return orderRepository.save(order);
    }

    public Order findById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    public void updateOrder(String orderId, TossPaymentStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        order.setStatus(status);
        orderRepository.save(order);
    }
}