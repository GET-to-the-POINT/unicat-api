package taeniverse.unicatApi.mvc.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller                           // 컨트롤러임을 선언, 뷰 반환 시 사용
@RequestMapping("/api/payment")          // "/payment" 경로로 요청이 들어오면 이 컨트롤러가 처리
public class PaymentPageController {

    /**
     * 결제 페이지를 렌더링하기 위한 GET 요청 처리 메서드입니다.
     * @param model 뷰에 전달할 모델 데이터
     * @return "payment" 뷰 이름 (resources/templates/payment.html)
     */
    @GetMapping                       // GET 방식으로 요청 받음
    public String showPaymentPage(Model model) {
        // 클라이언트에서 Toss Payments SDK를 초기화하는 데 필요한 clientKey를 모델에 추가
        model.addAttribute("clientKey", "test_gck_docs_Ovk5rk1EwkEbP0W43n07xlzm");
        // 고객 고유값(테스트용)인 customerKey를 모델에 추가
        model.addAttribute("customerKey", "vxuLWeb70slvL27O1Svpj");


        return "payment";
    }
}
