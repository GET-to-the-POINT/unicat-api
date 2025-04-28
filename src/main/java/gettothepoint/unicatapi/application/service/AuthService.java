package gettothepoint.unicatapi.application.service;

import gettothepoint.unicatapi.email.infrastructure.email.SyncMailSender;
import gettothepoint.unicatapi.application.service.member.MemberService;
import gettothepoint.unicatapi.common.util.JwtUtil;
import gettothepoint.unicatapi.domain.dto.sign.SignInRequest;
import gettothepoint.unicatapi.domain.dto.sign.SignUpRequest;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.payment.Plan;
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

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MessageSource messageSource;
    private final MemberService memberService;
    private final SyncMailSender syncMailSender;

    public String signUp(SignUpRequest signUpRequest) {
        Member member = memberService.create(signUpRequest.email(), signUpRequest.password(), signUpRequest.name(), signUpRequest.phoneNumber());
//        emailService.sendVerificationEmail(member);
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
        return jwtUtil.generateJwtToken(member.getId(), member.getEmail(), currentPlan.getName());
    }
}

