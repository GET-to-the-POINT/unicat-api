package getToThePoint.unicatApi.presentation.controller.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import getToThePoint.unicatApi.common.propertie.AppProperties;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final AppProperties appProperties;

    @GetMapping("/todos")
    public String home() {
        return "todos";
    }

    @GetMapping("/api/payment")
    public String showPaymentPage(Model model) {
        model.addAttribute("clientKey", appProperties.toss().clientKey());
        String customerKey = UUID.randomUUID().toString();
        model.addAttribute("customerKey", customerKey);

        return "payment";
    }
}
