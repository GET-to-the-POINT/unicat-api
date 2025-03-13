package gettothepoint.unicatapi.presentation.controller.email;

import gettothepoint.unicatapi.application.service.MemberService;
import gettothepoint.unicatapi.application.service.email.EmailService;
import gettothepoint.unicatapi.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/email")
public class EmailController {

    private final EmailService emailService;
    private final MemberService memberService;
    private final JwtUtil jwtUtil;

    @GetMapping("/verifyEmail")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmail(@RequestParam("token") String token) {
        String email = jwtUtil.getEmailFromToken(token);
        memberService.verifyEmail(email);
    }
}
