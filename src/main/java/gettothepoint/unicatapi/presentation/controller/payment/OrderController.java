package gettothepoint.unicatapi.presentation.controller.payment;

import gettothepoint.unicatapi.application.service.payment.OrderService;
import gettothepoint.unicatapi.domain.dto.payment.SubscriptionRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(
            summary = "주문 생성",
            description = "사용자의 이메일과 구독 플랜을 기반으로 주문을 생성합니다. " +
                    "가능한 구독 플랜은 BASIC, PREMIUM, VIP 입니다."
    )

    @PostMapping("/orders")
    public void createOrder(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SubscriptionRequest request
    ) {
        String email = jwt.getClaimAsString("email");
        orderService.create(email, request.getPlan());
    }
}

