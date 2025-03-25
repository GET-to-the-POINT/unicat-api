package gettothepoint.unicatapi.application.service;

import gettothepoint.unicatapi.application.service.email.EmailService;
import gettothepoint.unicatapi.application.service.member.MemberService;
import gettothepoint.unicatapi.common.util.JwtUtil;
import gettothepoint.unicatapi.domain.dto.sign.SignInDto;
import gettothepoint.unicatapi.domain.dto.sign.SignUpRequest;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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

    public String signUp(SignUpRequest signUpRequest) {
        validateEmail(signUpRequest.email());
        Member member = memberService.create(signUpRequest.email(), signUpRequest.password(), signUpRequest.name(), signUpRequest.phoneNumber());
        emailService.sendVerificationEmail(member);
        return generateAndAddJwtToken(member);
    }

    public String signIn(SignInDto signInDto) {
        Member member = validateCredentials(signInDto.email(), signInDto.password());
        return generateAndAddJwtToken(member);
    }

    public void signOut(HttpServletResponse response) {
        Cookie jwtCookie = jwtUtil.removeJwtCookie();
        response.addCookie(jwtCookie);
    }

    private void validateEmail(String email) {
        if (Boolean.TRUE.equals(memberRepository.existsByEmail(email))) {
            String errorMessage = messageSource.getMessage("error.email.in.use", null, "", LocaleContextHolder.getLocale());
            throw new ResponseStatusException(HttpStatus.CONFLICT, errorMessage);
        }
    }

    private Member validateCredentials(String email, String password) {
        Member member = memberRepository.findByEmail(email).orElse(null);
        if (member == null || !passwordEncoder.matches(password, member.getPassword())) {
            String errorMessage = messageSource.getMessage("error.invalid.credentials", null, "", LocaleContextHolder.getLocale());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, errorMessage);
        }
        return member;
    }

    private String generateAndAddJwtToken(Member member) {
        return jwtUtil.generateJwtToken(member.getId(), member.getEmail(), member.getSubscription().getSubscriptionPlan());
    }

}
