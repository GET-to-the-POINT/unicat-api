package gettothepoint.unicatapi.auth.application;

import gettothepoint.unicatapi.common.properties.ApiProperties;
import gettothepoint.unicatapi.common.util.JwtUtil;
import gettothepoint.unicatapi.auth.presentation.SignInRequest;
import gettothepoint.unicatapi.auth.presentation.SignUpRequest;
import gettothepoint.unicatapi.mail.MailService;
import gettothepoint.unicatapi.member.application.MemberService;
import gettothepoint.unicatapi.member.domain.Member;
import gettothepoint.unicatapi.subscription.domain.Plan;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MessageSource messageSource;
    private final MemberService memberService;
    private final MailService mailService;

    public String signUp(SignUpRequest signUpRequest, URI uri) {
        Member member = memberService.create(signUpRequest.email(), signUpRequest.password(), signUpRequest.name(), signUpRequest.phoneNumber());
        mailService.confirmEmail(member, uri);
        return generateAndAddJwtToken(member);
    }

    public String signIn(SignInRequest signInRequest) {
        Member member = memberService.getOrElseThrow(signInRequest.email());
        validateCredentials(member, signInRequest.password());
        return generateAndAddJwtToken(member);
    }

    private void validateCredentials(Member member, String password) {
        if (!passwordEncoder.matches(password, member.getPassword())) {
            String errorMessage = messageSource.getMessage("error.invalid.credentials", null, "", LocaleContextHolder.getLocale());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, errorMessage);
        }
    }

    private String generateAndAddJwtToken(Member member) {
        Plan currentPlan = member.getSubscription().getPlan();
        if (currentPlan == null) {
            throw new IllegalStateException("활성화된 플랜이 없습니다.");
        }
        return jwtUtil.generateJwtToken(member.getId());
    }
}

