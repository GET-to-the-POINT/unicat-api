package gettothepoint.unicatapi.presentation.controller.member;

import gettothepoint.unicatapi.application.service.member.MemberService;
import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class EmailController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;
    private final AppProperties appProperties;

    @GetMapping("/email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmail(@RequestParam("token") String token, HttpServletResponse response) throws IOException {
        String email = jwtUtil.getEmail(token);
        memberService.verifyEmail(email);

        String frontend = appProperties.frontend().url();
        response.sendRedirect(frontend);
    }
}
