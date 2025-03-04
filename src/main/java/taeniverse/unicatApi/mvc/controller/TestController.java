package taeniverse.unicatApi.mvc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sample")
public class TestController {
    @GetMapping("/oauth2/google")
    public ResponseEntity<String> uploadVideo(
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        return ResponseEntity.ok("Access token: " + accessToken);
    }

    @GetMapping("/principal")
    public ResponseEntity<String> getPrincipal(@AuthenticationPrincipal Object principal) {
        return switch (principal) {
            case null -> ResponseEntity.ok("Principal is null");
            case OAuth2User oAuth2User -> ResponseEntity.ok("Principal is an OAuth2User");
            case UserDetails userDetails -> ResponseEntity.ok("Principal is a UserDetails");
            default -> ResponseEntity.ok("Principal is of unknown type: " + principal.getClass().getName());
        };
    }
}
