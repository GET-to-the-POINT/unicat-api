package gettothepoint.unicatapi.presentation.controller.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import gettothepoint.unicatapi.domain.dto.payment.OrderRequest;
import gettothepoint.unicatapi.application.service.payment.OrderService;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping(value = "/order")
    public void createOrder(@AuthenticationPrincipal Jwt jwt) {

        OrderRequest orderRequest = new OrderRequest("월간 구독", 10000L);

        Long memberId = Long.valueOf(jwt.getSubject());

        orderService.createOrder(orderRequest, memberId);
    }
}
