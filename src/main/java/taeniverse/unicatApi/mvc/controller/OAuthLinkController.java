package taeniverse.unicatApi.mvc.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import taeniverse.unicatApi.component.propertie.AppProperties;
import taeniverse.unicatApi.mvc.model.dto.oauth.OAuthLinkDto;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/oauth-links")
@RequiredArgsConstructor
@Tag(name = "OAuth Links", description = "OAuth 관련 링크들을 제공하는 API")
public class OAuthLinkController {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final AppProperties appProperties;

    @GetMapping
    @Operation(
            summary = "OAuth 인증 링크 목록 조회",
            description = "OAuth 인증에 필요한 각 공급자별 링크 목록을 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공적으로 OAuth 링크 목록을 조회함",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = OAuthLinkDto.class))
                            )
                    )
            }
    )
    public List<OAuthLinkDto> getOAuthHrefLinks() {
        List<OAuthLinkDto> oAuthLinkDtos = new ArrayList<>();

        Iterable<ClientRegistration> registrations =
                ((InMemoryClientRegistrationRepository) clientRegistrationRepository);

        String baseUrl = appProperties.api().protocol() + "://" + appProperties.api().domain() + ":" + appProperties.api().port();
        registrations.forEach(registration -> {
            String href = "/oauth2/authorization/" + registration.getRegistrationId();
            OAuthLinkDto oAuthLinkDto = OAuthLinkDto.builder()
                    .provider(registration.getClientName())
                    .link(baseUrl + href)
                    .build();
            oAuthLinkDtos.add(oAuthLinkDto);
        });
        return oAuthLinkDtos;
    }
}