package gettothepoint.unicatapi.presentation.controller.member.password;

import gettothepoint.unicatapi.application.service.member.MemberService;
import gettothepoint.unicatapi.application.service.member.password.PasswordService;
import gettothepoint.unicatapi.common.util.JwtUtil;
import gettothepoint.unicatapi.domain.dto.password.AnonymousChangePasswordRequest;
import gettothepoint.unicatapi.domain.dto.password.AuthorizedChangePasswordRequest;
import gettothepoint.unicatapi.domain.dto.password.PasswordResetEmailRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
@Tag(name = "Member - Password API", description = "비밀번호 관련 API")
public class PasswordController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;
    private final PasswordService passwordService;

    @PatchMapping("/me/password")
    public void resetPasswordForLoggedInUser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AuthorizedChangePasswordRequest request) {
        String email = jwt.getClaim("email");
        memberService.updatePassword(email, request.newPassword());
    }

    @PatchMapping("/anonymous/password")
    public void resetPasswordForNonLoggedInUser(
            @Valid @RequestBody AnonymousChangePasswordRequest request) {
        String email = jwtUtil.getEmailFromToken(request.token());
        memberService.updatePassword(email, request.newPassword());
    }

    @PostMapping("/anonymous/password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void sendPasswordResetEmail(@Valid @RequestBody PasswordResetEmailRequest request) {
        passwordService.sendResetEmail(request.email(), request.url());
    }
}

