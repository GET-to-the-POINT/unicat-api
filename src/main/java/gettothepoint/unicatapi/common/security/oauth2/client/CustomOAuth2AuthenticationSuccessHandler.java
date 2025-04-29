package gettothepoint.unicatapi.common.security.oauth2.client;

import gettothepoint.unicatapi.common.security.oauth2.client.authorizedclient.HttpCookieOAuth2AuthorizationRequestRepository;
import gettothepoint.unicatapi.common.util.CookieUtil;
import gettothepoint.unicatapi.common.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomOAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        Long memberId = oAuth2User.getAttribute("memberId");
        assert memberId != null;
        String token = jwtUtil.generateJwtToken(memberId);
        Cookie jwtCookie = cookieUtil.createJwtCookie(token);
        response.addCookie(jwtCookie);

        // 꺼낸뒤 버린다.
        Cookie redirectCookie = WebUtils.getCookie(request, HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT);
        String redirectUrl = "/";
        if (redirectCookie != null) {
            redirectUrl = redirectCookie.getValue();
            cookieUtil.zeroAge(redirectCookie);
        }

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}