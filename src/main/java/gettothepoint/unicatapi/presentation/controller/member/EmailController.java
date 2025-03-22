package gettothepoint.unicatapi.presentation.controller.member;

import gettothepoint.unicatapi.application.service.member.MemberService;
import gettothepoint.unicatapi.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class EmailController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;

    @GetMapping("/email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmail(@RequestParam("token") String token) {
        String email = jwtUtil.getEmailFromToken(token);
        memberService.verifyEmail(email);
    }
}
