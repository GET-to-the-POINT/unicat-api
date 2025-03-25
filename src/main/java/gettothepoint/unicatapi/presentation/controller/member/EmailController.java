package gettothepoint.unicatapi.presentation.controller.member;

import gettothepoint.unicatapi.application.service.member.MemberService;
import gettothepoint.unicatapi.common.util.JwtUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = " - Email", description = "이메일 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class EmailController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;

    @GetMapping("/email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmail(@RequestParam("token") String token) {
        String email = jwtUtil.getEmail(token);
        memberService.verifyEmail(email);
    }
}
