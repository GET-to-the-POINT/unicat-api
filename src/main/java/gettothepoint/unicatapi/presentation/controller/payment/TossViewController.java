package gettothepoint.unicatapi.presentation.controller.payment;

import gettothepoint.unicatapi.common.properties.TossProperties;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Payment", description = "Toss Payments API")
@Controller
@RequestMapping("/toss")
@RequiredArgsConstructor
public class TossViewController {

    private final TossProperties tossProperties;

    @GetMapping
    public String showPaymentPage(Model model, @AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaim("email");
        model.addAttribute("clientKey", tossProperties.clientKey());
        model.addAttribute("customerKey", email);
        return "payment";
    }
}
