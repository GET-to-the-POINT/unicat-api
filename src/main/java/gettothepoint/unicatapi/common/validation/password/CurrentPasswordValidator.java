package gettothepoint.unicatapi.common.validation.password;

import gettothepoint.unicatapi.member.application.MemberService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurrentPasswordValidator implements ConstraintValidator<CurrentPasswordMatches, String> {

    private final MemberService memberService;

    @Override
    public boolean isValid(String currentPassword, ConstraintValidatorContext context) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthToken) {
            Jwt jwt = jwtAuthToken.getToken();
            Long memberId = Long.parseLong(jwt.getSubject());
            return memberService.validCurrentPassword(memberId, currentPassword);
        }

        return false;
    }
}