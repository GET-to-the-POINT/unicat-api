package gettothepoint.unicatapi.domain.dto.project;


import jakarta.validation.constraints.NotBlank;

public record PromptRequest(@NotBlank String prompt) {
}
