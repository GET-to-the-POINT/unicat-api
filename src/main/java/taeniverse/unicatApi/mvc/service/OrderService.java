package taeniverse.unicatApi.mvc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import taeniverse.unicatApi.mvc.model.dto.OrderRequest;
import taeniverse.unicatApi.mvc.model.entity.Member;
import taeniverse.unicatApi.mvc.model.entity.Order;
import taeniverse.unicatApi.mvc.repository.OrderRepository;
import taeniverse.unicatApi.payment.TossPaymentStatus;


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