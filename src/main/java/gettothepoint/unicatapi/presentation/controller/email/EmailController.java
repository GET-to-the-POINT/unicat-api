package gettothepoint.unicatapi.presentation.controller.email;

import gettothepoint.unicatapi.application.service.MemberService;
import gettothepoint.unicatapi.application.service.email.EmailService;
import gettothepoint.unicatapi.common.util.JwtUtil;
import gettothepoint.unicatapi.domain.dto.email.EmailRequestDto;
import jakarta.validation.Valid;
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

    @PostMapping("/sendEmail")
    public void mailSend(@RequestBody @Valid EmailRequestDto requestDto) {
        Long memberId = memberService.findByEmail(requestDto.getEmail()).getId();
        emailService.sendVerificationEmail(requestDto.getEmail(), memberId);
    }

    @GetMapping("/verifyEmail")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmail(@RequestParam("token") String token) {
        String email = jwtUtil.getEmailFromToken(token);
        memberService.verifyEmail(email);
    }
}
