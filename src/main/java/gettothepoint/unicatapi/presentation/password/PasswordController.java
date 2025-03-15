package gettothepoint.unicatapi.presentation.password;

import gettothepoint.unicatapi.application.service.password.PasswordService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class PasswordController {

    private final PasswordService passwordService;

    @PostMapping("/me/password/verify")
    public void verifyCurrentPassword(
            @AuthenticationPrincipal Jwt jwt,
            @NotEmpty @RequestParam String currentPassword) {
        String email = jwt.getClaimAsString("email");
        passwordService.verifyCurrentPassword(email, currentPassword);
    }

}

