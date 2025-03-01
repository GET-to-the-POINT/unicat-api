package taeniverse.unicatApi.mvc.model.dto.sign;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonDeserialize(builder = SignInDto.SignInDtoBuilder.class)
@Builder
public class SignInDto {

    @NotBlank(message = "{email.required}")
    private final String email;

    @NotBlank(message = "{password.required}")
    private final String password;

    @JsonPOJOBuilder(withPrefix = "")
    public static class SignInDtoBuilder {
        // Lombok이 자동으로 빌더 메서드를 생성합니다.
    }
}