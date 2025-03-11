package gettothepoint.unicatapi.presentation.controller.payment;

import gettothepoint.unicatapi.application.service.MemberService;
import gettothepoint.unicatapi.domain.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import gettothepoint.unicatapi.common.propertie.AppProperties;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final AppProperties appProperties;
    private final MemberService memberService;

    @GetMapping("/todos")
    public String home() {
        return "todos";
    }

    @GetMapping("/api/payment")
    public String showPaymentPage(Model model, @AuthenticationPrincipal Jwt jwt) {
        model.addAttribute("clientKey", appProperties.toss().clientKey());

        String email = jwt.getClaim("email");
        Member member = memberService.findByEmail(email);
        String customerKey = member.getCustomerKey();

        model.addAttribute("customerKey", customerKey);
        return "payment";
    }
}
