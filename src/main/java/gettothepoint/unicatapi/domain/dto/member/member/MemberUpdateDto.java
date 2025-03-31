package gettothepoint.unicatapi.domain.dto.member.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원 정보 수정에 필요한 데이터 DTO")
public record MemberUpdateDto(

        @NotBlank(message = "{name.required}")
        @Schema(description = "회원 이름", example = "홍길동")
        String name,

        @NotBlank(message = "{phone.required}")
        @Schema(description = "회원 전화번호 (전체 번호 입력)", example = "01012345678")
        String phoneNumber

) {}