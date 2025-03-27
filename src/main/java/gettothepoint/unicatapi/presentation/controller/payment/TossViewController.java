package gettothepoint.unicatapi.presentation.controller.payment;

import gettothepoint.unicatapi.common.propertie.AppProperties;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(
        summary = "Toss 결제 페이지",
        description = "Toss 결제 진행을 위한 페이지를 렌더링합니다. 모델에 클라이언트키 및 고객의 이메일(고객 키)를 포함시켜 뷰를 반환합니다."
    )
    public String showPaymentPage(Model model,@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaim("email");
        model.addAttribute("clientKey", appProperties.toss().clientKey());
        model.addAttribute("customerKey", email);
        return "payment";
    }
}
