package gettothepoint.unicatapi.presentation.controller.member;

import gettothepoint.unicatapi.application.service.member.MemberService;
import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class EmailController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;
    private final AppProperties appProperties;

    @GetMapping("/email")
    @Operation(
        summary = "이메일 인증",
        description = "인증 토큰을 기반으로 사용자의 이메일을 검증하고, 인증 처리 후 프론트엔드 페이지로 리다이렉션합니다."
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmail(@RequestParam("token") String token, HttpServletResponse response) throws IOException {
        String email = jwtUtil.getEmail(token);
        memberService.verifyEmail(email);

        String frontend = appProperties.frontend().url();
        response.sendRedirect(frontend);
    }
}
