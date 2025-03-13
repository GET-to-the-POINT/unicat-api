package gettothepoint.unicatapi.presentation.controller.passwordreset;

import gettothepoint.unicatapi.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PasswordResetPageController {
    private final JwtUtil jwtUtil;
    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam("token") String token, Model model) {
        String email = jwtUtil.getEmailFromToken(token);
        model.addAttribute("email", email);
        return "reset-password";
    }
}