package gettothepoint.unicatapi.domain.dto.email;

import gettothepoint.unicatapi.domain.entity.VerificationCode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerificationCodeDto {
    private String email;
    private String code;

    public static VerificationCodeDto fromEntity(VerificationCode entity) {
        return VerificationCodeDto.builder()
                .email(entity.getEmail())
                .code(entity.getCode())
                .build();
    }
}