package gettothepoint.unicatapi.presentation.controller.password;

import gettothepoint.unicatapi.application.service.MemberService;
import gettothepoint.unicatapi.application.service.password.PasswordService;
import gettothepoint.unicatapi.common.util.JwtUtil;
import gettothepoint.unicatapi.domain.dto.password.AnonymousChangePasswordRequest;
import gettothepoint.unicatapi.domain.dto.password.AuthorizedChangePasswordRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class PasswordController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;
    private final PasswordService passwordService;

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.OK)
    public void resetPasswordForLoggedInUser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AuthorizedChangePasswordRequest request) {
        String email = jwt.getClaim("email");
        memberService.updatePassword(email, request.newPassword());
    }

    @PutMapping("/anonymous/password")
    @ResponseStatus(HttpStatus.OK)
    public void resetPasswordForNonLoggedInUser(
            @Valid @RequestBody AnonymousChangePasswordRequest request) {
        String email = jwtUtil.getEmailFromToken(request.token());
        memberService.updatePassword(email, request.newPassword());
    }
}

