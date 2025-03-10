package gettothepoint.unicatapi.presentation.controller.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import gettothepoint.unicatapi.domain.dto.payment.OrderRequest;
import gettothepoint.unicatapi.domain.entity.Member;
import gettothepoint.unicatapi.domain.entity.Order;
import gettothepoint.unicatapi.application.service.MemberService;
import gettothepoint.unicatapi.application.service.payment.OrderService;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MemberService memberService;

    @PostMapping(value = "/order")
    public Order createOrder(@RequestBody OrderRequest orderRequest,
                                @AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaim("email");
        Member member = memberService.findByEmail(email);
        return orderService.create(orderRequest, member);
    }
 }

