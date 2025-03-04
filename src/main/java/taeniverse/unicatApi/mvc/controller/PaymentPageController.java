package taeniverse.unicatApi.mvc.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;


//결제 페이지(Thymeleaf)를 반환하는 컨트롤러입니다.
@Controller
@RequestMapping("/api/payment")
public class PaymentPageController {

    @Value("${client.key}")
    private String clientKey;
    
  // GET 방식으로 요청 받음
    @GetMapping
    public String showPaymentPage(Model model) {
        // 클라이언트에서 Toss Payments SDK를 초기화하는 데 필요한 clientKey를 모델에 추가
        model.addAttribute("clientKey",  clientKey);
        // 고객 고유값(테스트용)인 customerKey를 모델에 추가
        String customerKey = UUID.randomUUID().toString();
        model.addAttribute("customerKey", customerKey);

        // payment.html 템플릿을 반환 (Thymeleaf가 resources/templates/payment.html 파일을 찾음)
        return "payment";
    }
}