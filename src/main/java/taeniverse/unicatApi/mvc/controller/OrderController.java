package taeniverse.unicatApi.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import taeniverse.unicatApi.mvc.model.dto.OrderRequest;
import taeniverse.unicatApi.mvc.model.entity.Member;
import taeniverse.unicatApi.mvc.model.entity.Order;
import taeniverse.unicatApi.mvc.service.MemberService;
import taeniverse.unicatApi.mvc.service.OrderService;

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

