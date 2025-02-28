package taeniverse.unicatApi.mvc.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import taeniverse.unicatApi.mvc.model.dto.OrderRequest;
import taeniverse.unicatApi.mvc.model.dto.OrderResponse;
import taeniverse.unicatApi.mvc.model.entity.Order;
import taeniverse.unicatApi.mvc.service.OrderService;
import taeniverse.unicatApi.payment.PayType;
import taeniverse.unicatApi.payment.TossPaymentStatus;
import taeniverse.unicatApi.temp.CustomOAuth2User;
import org.springframework.security.core.Authentication;


@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest,
                                                     Authentication authentication) {
        // 인증된 사용자 정보에서 CustomOAuth2User 객체를 얻습니다.
        CustomOAuth2User customUser = (CustomOAuth2User) authentication.getPrincipal();
        // CustomOAuth2User가 Member 엔티티를 포함하고 있다고 가정합니다.
        Long memberId = customUser.getMember().getId();

        String orderId = orderService.createOrUpdateOrder(orderRequest, memberId);
        OrderResponse response = new OrderResponse(memberId, orderId, "Order created successfully");

        log.info("Order created successfully for memberId: {}, orderId: {}", memberId, orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable String orderId) {
        Order order = orderService.getOrder(orderId);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam TossPaymentStatus newStatus) {
        // PathVariable orderId와 RequestParam newStatus를 사용하여 finalizeOrder 호출
        Order updatedOrder = orderService.finalizeOrder(orderId, newStatus, PayType.CARD);
        log.info("Order status updated: {} -> {}", orderId, newStatus);
        return ResponseEntity.ok(updatedOrder);
    }
}
