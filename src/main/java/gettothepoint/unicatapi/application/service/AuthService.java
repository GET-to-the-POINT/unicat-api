package gettothepoint.unicatapi.application.service;

import gettothepoint.unicatapi.application.service.email.EmailService;
import gettothepoint.unicatapi.common.util.JwtUtil;
import gettothepoint.unicatapi.domain.dto.sign.SignInDto;
import gettothepoint.unicatapi.domain.dto.sign.SignUpDto;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.repository.MemberRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MessageSource messageSource;
    private final MemberService memberService;
    private final EmailService emailService;

    public void signUp(SignUpDto signUpDto, HttpServletResponse response) {
        validateEmail(signUpDto.email());
        Member member = createMember(signUpDto.email(), signUpDto.password());
        generateAndAddJwtToken(response, member);
        emailService.sendVerificationEmail(member);
    }

    public void signIn(SignInDto signInDto, HttpServletResponse response) {
        Member member = validateCredentials(signInDto.email(), signInDto.password());
        String token = generateAndAddJwtToken(response, member);
    }

    public void signOut(HttpServletResponse response) {
        jwtUtil.removeJwtCookie(response);
    }

    private void validateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            String errorMessage = messageSource.getMessage("error.email.in.use", null, "", LocaleContextHolder.getLocale());
            throw new ResponseStatusException(HttpStatus.CONFLICT, errorMessage);
        }
    }

    private Member createMember(String email, String password) {
        return memberService.create(email, passwordEncoder.encode(password));
    }

    private Member validateCredentials(String email, String password) {
        Member member = memberRepository.findByEmail(email).orElse(null);
        if (member == null || !passwordEncoder.matches(password, member.getPassword())) {
            String errorMessage = messageSource.getMessage("error.invalid.credentials", null, "", LocaleContextHolder.getLocale());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, errorMessage);
        }
        return member;
    }

    private String generateAndAddJwtToken(HttpServletResponse response, Member member) {
        String token = jwtUtil.generateJwtToken(member.getId(), member.getEmail());
        jwtUtil.addJwtCookie(response, token);
        return token;
    }

}
