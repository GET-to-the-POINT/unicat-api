package gettothepoint.unicatapi.payment.presentation;

import gettothepoint.unicatapi.common.properties.TossProperties;
import gettothepoint.unicatapi.member.application.MemberService;
import gettothepoint.unicatapi.member.domain.Member;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Tag(name = "Payment", description = "Toss Payments API")
@Controller
@RequestMapping("/toss")
@RequiredArgsConstructor
public class TossViewController {

    private final TossProperties tossProperties;
    private final MemberService memberService;

    @GetMapping
    public String showPaymentPage(Model model, @AuthenticationPrincipal Jwt jwt) {
        String memberId = jwt.getSubject();

        Member member = memberService.getOrElseThrow(UUID.fromString(memberId));

        model.addAttribute("clientKey", tossProperties.clientKey());
        model.addAttribute("customerKey", memberId);
        model.addAttribute("customerName", member.getName());
        model.addAttribute("customerEmail", member.getEmail());

        return "payment";
    }

    @GetMapping("/register")
    public String redirectToPaymentPage() {
        return "redirect:/toss";
    }
}
