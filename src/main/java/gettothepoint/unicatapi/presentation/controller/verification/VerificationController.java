package gettothepoint.unicatapi.presentation.controller.verification;

import gettothepoint.unicatapi.application.service.AuthService;
import gettothepoint.unicatapi.application.service.MemberService;
import gettothepoint.unicatapi.application.service.password.PasswordService;
import gettothepoint.unicatapi.common.util.JwtUtil;
import gettothepoint.unicatapi.domain.dto.password.ChangePasswordDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/verification")
public class VerificationController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final PasswordService passwordService;

    @GetMapping("/email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmail(@RequestParam("token") String token) {
        String email = jwtUtil.getEmailFromToken(token);
        memberService.verifyEmail(email);
    }

    @PostMapping("/email/resend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resendVerificationEmail(@RequestParam("token") String expiredToken) {
        authService.resendVerificationEmailFromExpiredToken(expiredToken);
    }

    @PostMapping("/password/reset-request")
    public ResponseEntity<Void> requestPasswordReset(@RequestParam String email) {
        passwordService.sendPasswordResetEmail(email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(@RequestParam String email,
                                              @Valid @ModelAttribute ChangePasswordDto changePasswordDto) {
        passwordService.resetPassword(email, changePasswordDto.newPassword(), changePasswordDto.confirmNewPassword());
        return ResponseEntity.noContent().build();
    }
}

