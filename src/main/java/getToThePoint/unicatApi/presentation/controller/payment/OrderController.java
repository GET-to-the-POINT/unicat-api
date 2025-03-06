package getToThePoint.unicatApi.presentation.controller.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import getToThePoint.unicatApi.domain.dto.payment.OrderRequest;
import getToThePoint.unicatApi.domain.entity.Member;
import getToThePoint.unicatApi.domain.entity.Order;
import getToThePoint.unicatApi.application.service.MemberService;
import getToThePoint.unicatApi.application.service.payment.OrderService;

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

