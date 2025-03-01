package taeniverse.unicatApi.mvc.model.dto.sign;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonDeserialize(builder = SignUpDto.SignUpDtoBuilder.class)
@Builder
public class SignUpDto {

    @NotBlank(message = "{email.required}")
    @Email(message = "{email.valid}")
    private final String email;

    @NotBlank(message = "{password.required}")
    private final String password;

    @JsonPOJOBuilder(withPrefix = "")
    public static class SignUpDtoBuilder {
        // Lombok이 자동으로 빌더 메서드(build(), email(), password() 등)를 생성합니다.
    }
}