package gettothepoint.unicatapi.member.presentation;

import gettothepoint.unicatapi.member.application.MemberService;
import gettothepoint.unicatapi.member.application.PasswordService;
import gettothepoint.unicatapi.common.util.JwtUtil;
import gettothepoint.unicatapi.member.domain.dto.password.AnonymousChangePasswordRequest;
import gettothepoint.unicatapi.member.domain.dto.password.AuthorizedChangePasswordRequest;
import gettothepoint.unicatapi.member.domain.dto.password.PasswordResetEmailRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
@Tag(name = "Member", description = "비밀번호 관련 API")
public class PasswordController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;
    private final PasswordService passwordService;

    @PatchMapping("/me/password")
    @Operation(
        summary = "로그인 사용자 비밀번호 변경",
        description = "로그인한 사용자의 현재 비밀번호를 변경합니다. JWT 토큰의 subject 값을 사용하여 memberId를 확인합니다."
    )
    public void resetPasswordForLoggedInUser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AuthorizedChangePasswordRequest request) {
        Long memberId = Long.parseLong(jwt.getSubject());
        memberService.updatePassword(memberId, request.newPassword());
    }

    @PatchMapping("/anonymous/password")
    @Operation(
        summary = "비로그인 사용자 비밀번호 변경",
        description = "비로그인 사용자의 비밀번호를 변경합니다. 이메일 인증 토큰을 사용하여 memberId를 확인합니다."
    )
    public void resetPasswordForNonLoggedInUser(
            @Valid @RequestBody AnonymousChangePasswordRequest request) {
        Long memberId = jwtUtil.getMemberId(request.token());
        memberService.updatePassword(memberId, request.newPassword());
    }

    @PostMapping("/anonymous/password")
    @Operation(
        summary = "비밀번호 재설정 이메일 전송",
        description = "입력된 이메일로 비밀번호 재설정을 위한 링크를 전송합니다. 요청 바디에 URL을 포함시켜 전송합니다."
    )
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void sendPasswordResetEmail(@Valid @RequestBody PasswordResetEmailRequest request) {
        passwordService.sendResetEmail(request.email(), request.url());
    }
}
