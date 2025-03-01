package taeniverse.unicatApi.mvc.model.dto.oauth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@Schema(description = "OAuth Link DTO")
public record OAuthLinkDto(
        @Schema(description = "OAuth Provider", example = "Google") String provider,
        @Schema(description = "OAuth Link", example = "https://example.com/oauth2/authorization/google") String link) { }