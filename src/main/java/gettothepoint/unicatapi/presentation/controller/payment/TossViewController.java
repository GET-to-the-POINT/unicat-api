package gettothepoint.unicatapi.presentation.controller.payment;

import gettothepoint.unicatapi.common.propertie.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class TossViewController {

    private final AppProperties appProperties;

    @GetMapping("/toss")
    public String showPaymentPage(Model model,@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaim("email");
        model.addAttribute("clientKey", appProperties.toss().clientKey());
        model.addAttribute("customerKey", email);
        return "payment";
    }
}
