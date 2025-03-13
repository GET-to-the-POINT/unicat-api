package gettothepoint.unicatapi.presentation.controller.passwordreset;

import gettothepoint.unicatapi.application.service.password.PasswordService;
import gettothepoint.unicatapi.domain.dto.password.ChangePasswordDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordService passwordService;

    @PostMapping("/reset-request")
    public void requestReset(@RequestParam String email) {
        passwordService.sendPasswordResetEmail(email);
    }

    @PostMapping("/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@RequestParam String email,
                              @Valid @ModelAttribute ChangePasswordDto changePasswordDto) {
       passwordService.resetPassword(
               email,
               changePasswordDto.newPassword(),
               changePasswordDto.confirmNewPassword());
    }
}
