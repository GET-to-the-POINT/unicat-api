package gettothepoint.unicatapi.presentation.controller.email;

import gettothepoint.unicatapi.application.service.MemberService;
import gettothepoint.unicatapi.domain.dto.email.EmailRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class EmailController {

    private final MemberService memberService;


    // 인증 번호 전송
    @PostMapping("/sendEmail")
    public ResponseEntity<String> mailSend(@RequestBody @Valid EmailRequestDto requestDto) {
        String response = memberService.sendVerificationEmail(requestDto.getEmail());
        return ResponseEntity.ok(response);
    }

    // 이메일 인증
    @GetMapping("/verifyEmail")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        String response = memberService.verifyEmail(token);
        return ResponseEntity.ok(response);
    }
}
