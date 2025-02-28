package taeniverse.unicatApi.mvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PaymentFailController {

    @GetMapping("/fail")
    public String paymentFail(
            @RequestParam(value = "message", required = false) String message,
            Model model) {

        model.addAttribute("message", message);
        return "fail"; // src/main/resources/templates/fail.html
    }
}