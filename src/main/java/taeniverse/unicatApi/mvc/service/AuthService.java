package taeniverse.unicatApi.mvc.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import taeniverse.unicatApi.mvc.model.dto.sign.SignInDto;
import taeniverse.unicatApi.mvc.model.dto.sign.SignUpDto;
import taeniverse.unicatApi.mvc.model.entity.Member;
import taeniverse.unicatApi.mvc.repository.MemberRepository;
import taeniverse.unicatApi.component.util.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public void signUp(SignUpDto signUpDto, HttpServletResponse response) {
        if (memberRepository.existsByEmail(signUpDto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
        }

        Member member = Member.builder().email(signUpDto.getEmail()).password(passwordEncoder.encode(signUpDto.getPassword())).build();

        memberRepository.save(member);

        String token = jwtUtil.generateJwtToken(member.getEmail());
        jwtUtil.addJwtCookie(response, token);
    }

    public void signIn(SignInDto signInDto, HttpServletResponse response) {
        Member member = memberRepository.findByEmail(signInDto.getEmail()).orElse(null);
        if (member == null || !passwordEncoder.matches(signInDto.getPassword(), member.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        String token = jwtUtil.generateJwtToken(member.getEmail());
        jwtUtil.addJwtCookie(response, token);
    }

    public void signOut(HttpServletResponse response) {
        jwtUtil.removeJwtCookie(response);
    }
}