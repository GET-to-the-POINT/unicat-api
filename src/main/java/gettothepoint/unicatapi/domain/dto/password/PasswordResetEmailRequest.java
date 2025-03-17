package gettothepoint.unicatapi.domain.dto.password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.hibernate.validator.constraints.URL;

@Builder
public record PasswordResetEmailRequest(

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일을 입력해주세요.")
    String email,

    @NotBlank(message = "URL은 필수입니다.")
    @URL(message = "유효한 URL을 입력해주세요.")
    String url
) {
}
