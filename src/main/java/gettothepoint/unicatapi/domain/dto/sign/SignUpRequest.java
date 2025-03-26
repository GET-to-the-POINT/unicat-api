package gettothepoint.unicatapi.domain.dto.sign;

import gettothepoint.unicatapi.common.validation.fieldcompare.CompareResult;
import gettothepoint.unicatapi.common.validation.fieldcompare.CompareTarget;
import gettothepoint.unicatapi.common.validation.fieldcompare.FieldComparison;
import gettothepoint.unicatapi.common.validation.email.UniqueEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Schema(description = "사용자 회원가입에 필요한 데이터 DTO")
@Builder
@FieldComparison(message = "패스워드와 확인 패스워드가 일치하지 않습니다.")
@Jacksonized
public record SignUpRequest(
        @NotBlank(message = "{email.required}")
        @Schema(description = "사용자 이메일", example = "user@example.com")
        @Email(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "{email.valid}")
        @UniqueEmail(message = "이미 등록된 이메일입니다.")
        String email,

        @NotBlank(message = "{password.required}")
        @Schema(description = "사용자 비밀번호", example = "Strong@123")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$",
                message = "비밀번호는 최소 8자, 최대 16자, 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다."
        )
        @CompareTarget // 패스워드 비교 대상 필드 지정
        String password ,

        @Schema(description = "비밀번호 확인", example = "Strong@123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "패스워드 확인을 입력해주세요.")
        @CompareResult // 패스워드 비교 결과 필드 지정
        String confirmPassword,

        @NotBlank(message = "{name.required}")
        @Schema(description = "사용자 이름", example = "홍길동")
        String  name,

        @NotBlank(message = "{phone.required}")
        @Schema(description = "사용자 전화번호 (전체 번호 입력)", example = "01012345678")
        String phoneNumber

) {}