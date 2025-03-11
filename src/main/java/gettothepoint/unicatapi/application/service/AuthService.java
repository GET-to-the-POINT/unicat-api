package gettothepoint.unicatapi.application.service;

import gettothepoint.unicatapi.common.util.JwtUtil;
import gettothepoint.unicatapi.common.util.EmailValidator;
import gettothepoint.unicatapi.domain.dto.sign.SignInDto;
import gettothepoint.unicatapi.domain.dto.sign.SignUpDto;
import gettothepoint.unicatapi.domain.entity.Member;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final EmailValidator emailValidator;
    private final MemberService memberService;

    public void signUp(SignUpDto signUpDto, HttpServletResponse response) {
        emailValidator.validateEmail(signUpDto.email());
        Member member = memberService.create(signUpDto.email(), signUpDto.password());
        jwtUtil.generateAndAddJwtToken(response, member);
    }

    public void signIn(SignInDto signInDto, HttpServletResponse response) {
        Member member = memberService.validateCredentials(signInDto.email(), signInDto.password());
        jwtUtil.generateAndAddJwtToken(response, member);
    }

    public void signOut(HttpServletResponse response) {
        jwtUtil.removeJwtCookie(response);
    }
}
