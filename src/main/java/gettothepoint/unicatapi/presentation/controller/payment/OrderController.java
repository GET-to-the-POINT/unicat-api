package gettothepoint.unicatapi.presentation.controller.payment;

import gettothepoint.unicatapi.domain.dto.payment.OrderResponse;
import gettothepoint.unicatapi.domain.entity.payment.Order;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import gettothepoint.unicatapi.domain.dto.payment.OrderRequest;
import gettothepoint.unicatapi.application.service.payment.OrderService;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/order")
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody OrderRequest request
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());
        Order order = orderService.createOrder(request, memberId);

        OrderResponse response = OrderResponse.builder()
                .amount(order.getAmount())
                .customerKey(order.getMember().getCustomerKey())
                .orderId(order.getId())
                .orderName(order.getOrderName())
                .build();

        return ResponseEntity.ok(response);
    }
}

