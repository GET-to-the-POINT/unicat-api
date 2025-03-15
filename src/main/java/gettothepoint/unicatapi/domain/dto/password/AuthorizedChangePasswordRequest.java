package gettothepoint.unicatapi.domain.dto.password;

import gettothepoint.unicatapi.common.validation.fieldcompare.CompareResult;
import gettothepoint.unicatapi.common.validation.fieldcompare.CompareTarget;
import gettothepoint.unicatapi.common.validation.fieldcompare.FieldComparison;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Schema(description = "사용자 비밀번호 변경에 필요한 데이터 DTO")
@Builder
@FieldComparison(message = "새 비밀번호와 비밀번호 확인이 일치하지 않습니다.")
@Jacksonized
public record AuthorizedChangePasswordRequest(


        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        @Schema(description = "새 비밀번호", example = "NewStrongPassword123")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$",
                message = "새 비밀번호는 최소 8자, 최대 16자, 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다."
        )
        @CompareTarget
        String newPassword,

        @NotBlank(message = "비밀번호 확인을 입력해주세요.")
        @Schema(description = "비밀번호 확인", example = "NewStrongPassword123", requiredMode = Schema.RequiredMode.REQUIRED)
        @CompareResult
        String confirmNewPassword
) {}